"""
Example Synapse Plugin Implementation

This module demonstrates a complete plugin implementation with:
- Tool registration and execution
- Configuration management
- Lifecycle hooks
- State management
- Error handling
- Async operations
"""

import asyncio
import logging
from typing import Any, Dict, List, Optional
from datetime import datetime

try:
    import requests
    from pydantic import BaseModel, Field, validator
except ImportError:
    # Fallback for environments without dependencies
    requests = None
    BaseModel = object
    Field = lambda **kwargs: None
    validator = lambda *args, **kwargs: lambda f: f


class PluginConfig(BaseModel):
    """Plugin configuration schema with validation"""
    
    api_key: str = Field(default="", description="API key for external service")
    endpoint: str = Field(default="https://api.example.com", description="API endpoint URL")
    timeout: int = Field(default=30, ge=1, le=300, description="Request timeout in seconds")
    max_retries: int = Field(default=3, ge=0, le=10, description="Maximum retry attempts")
    enable_caching: bool = Field(default=True, description="Enable response caching")
    log_level: str = Field(default="INFO", description="Logging level")
    
    @validator("log_level")
    def validate_log_level(cls, v):
        valid_levels = ["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"]
        if v.upper() not in valid_levels:
            raise ValueError(f"log_level must be one of {valid_levels}")
        return v.upper()
    
    @validator("endpoint")
    def validate_endpoint(cls, v):
        if not v.startswith(("http://", "https://")):
            raise ValueError("endpoint must start with http:// or https://")
        return v


class ExamplePlugin:
    """
    Example Synapse Plugin
    
    Demonstrates core plugin functionality including tool execution,
    lifecycle management, state handling, and error recovery.
    """
    
    def __init__(self, config: Optional[Dict[str, Any]] = None):
        """
        Initialize the plugin
        
        Args:
            config: Plugin configuration dictionary
        """
        self.config = PluginConfig(**(config or {}))
        self.logger = self._setup_logger()
        self.state: Dict[str, Any] = {
            "initialized_at": datetime.utcnow().isoformat(),
            "execution_count": 0,
            "cache": {} if self.config.enable_caching else None,
            "errors": []
        }
        self.session_data: Dict[str, Any] = {}
        
        self.logger.info(f"ExamplePlugin initialized with config: {self.config.dict()}")
    
    def _setup_logger(self) -> logging.Logger:
        """Configure plugin logger"""
        logger = logging.getLogger(f"synapse.plugin.{self.__class__.__name__}")
        logger.setLevel(getattr(logging, self.config.log_level))
        
        if not logger.handlers:
            handler = logging.StreamHandler()
            formatter = logging.Formatter(
                '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
            )
            handler.setFormatter(formatter)
            logger.addHandler(handler)
        
        return logger
    
    # -------------------------------------------------------------------------
    # Lifecycle Hooks
    # -------------------------------------------------------------------------
    
    async def on_startup(self) -> None:
        """Called when the plugin is loaded"""
        self.logger.info("Plugin starting up...")
        
        # Perform startup tasks
        if self.config.api_key:
            self.logger.info("API key configured")
        
        # Validate external dependencies
        if requests is None:
            self.logger.warning("requests library not available")
        
        self.logger.info("Plugin startup complete")
    
    async def on_shutdown(self) -> None:
        """Called when the plugin is being unloaded"""
        self.logger.info("Plugin shutting down...")
        
        # Cleanup resources
        if self.state.get("cache"):
            cache_size = len(self.state["cache"])
            self.logger.info(f"Clearing cache with {cache_size} entries")
            self.state["cache"].clear()
        
        # Log statistics
        self.logger.info(
            f"Total executions: {self.state['execution_count']}, "
            f"Errors: {len(self.state['errors'])}"
        )
        
        self.logger.info("Plugin shutdown complete")
    
    async def on_session_start(self, session_id: str, context: Dict[str, Any]) -> None:
        """
        Called when a new session starts
        
        Args:
            session_id: Unique session identifier
            context: Session context data
        """
        self.logger.info(f"Session started: {session_id}")
        self.session_data[session_id] = {
            "started_at": datetime.utcnow().isoformat(),
            "context": context,
            "tool_calls": []
        }
    
    async def on_session_end(self, session_id: str) -> None:
        """
        Called when a session ends
        
        Args:
            session_id: Session identifier
        """
        if session_id in self.session_data:
            session_info = self.session_data[session_id]
            tool_calls = len(session_info.get("tool_calls", []))
            self.logger.info(
                f"Session ended: {session_id}, Tool calls: {tool_calls}"
            )
            del self.session_data[session_id]
    
    async def on_tool_execute(
        self, 
        tool_name: str, 
        parameters: Dict[str, Any],
        session_id: Optional[str] = None
    ) -> None:
        """
        Called before a tool is executed
        
        Args:
            tool_name: Name of the tool being executed
            parameters: Tool parameters
            session_id: Associated session ID
        """
        self.logger.debug(f"Executing tool: {tool_name} with params: {parameters}")
        
        if session_id and session_id in self.session_data:
            self.session_data[session_id]["tool_calls"].append({
                "tool": tool_name,
                "timestamp": datetime.utcnow().isoformat(),
                "parameters": parameters
            })
    
    async def on_error(
        self, 
        error: Exception, 
        context: Dict[str, Any]
    ) -> None:
        """
        Called when an error occurs
        
        Args:
            error: The exception that occurred
            context: Error context information
        """
        error_info = {
            "timestamp": datetime.utcnow().isoformat(),
            "error": str(error),
            "type": type(error).__name__,
            "context": context
        }
        
        self.state["errors"].append(error_info)
        self.logger.error(f"Error occurred: {error}", exc_info=True)
    
    # -------------------------------------------------------------------------
    # Tool Implementations
    # -------------------------------------------------------------------------
    
    async def example_tool(
        self,
        query: str,
        options: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        Example tool that processes a query
        
        Args:
            query: Input query to process
            options: Optional processing options
                - format: Output format (json, text, xml)
                - verbose: Enable verbose output
        
        Returns:
            Processing results
        """
        self.logger.info(f"example_tool called with query: {query}")
        self.state["execution_count"] += 1
        
        options = options or {}
        output_format = options.get("format", "json")
        verbose = options.get("verbose", False)
        
        # Check cache
        cache_key = f"{query}:{output_format}:{verbose}"
        if self.config.enable_caching and cache_key in self.state.get("cache", {}):
            self.logger.debug(f"Cache hit for: {cache_key}")
            return self.state["cache"][cache_key]
        
        # Process query
        result = {
            "status": "success",
            "query": query,
            "format": output_format,
            "timestamp": datetime.utcnow().isoformat(),
            "processed": query.upper() if output_format == "text" else {
                "original": query,
                "length": len(query),
                "words": len(query.split())
            }
        }
        
        if verbose:
            result["metadata"] = {
                "execution_count": self.state["execution_count"],
                "cache_enabled": self.config.enable_caching,
                "plugin_version": "1.0.0"
            }
        
        # Update cache
        if self.config.enable_caching:
            self.state["cache"][cache_key] = result
        
        return result
    
    async def fetch_data(
        self,
        resource_id: str,
        fields: Optional[List[str]] = None
    ) -> Dict[str, Any]:
        """
        Fetch data from external API
        
        Args:
            resource_id: Resource identifier to fetch
            fields: Specific fields to retrieve
        
        Returns:
            Fetched data
        """
        self.logger.info(f"fetch_data called for resource: {resource_id}")
        self.state["execution_count"] += 1
        
        if not self.config.api_key:
            self.logger.warning("No API key configured, returning mock data")
            return {
                "status": "mock",
                "resource_id": resource_id,
                "fields": fields or [],
                "data": {"message": "Mock data - configure api_key for real data"}
            }
        
        if requests is None:
            raise RuntimeError("requests library not available")
        
        # Construct API request
        url = f"{self.config.endpoint}/resources/{resource_id}"
        headers = {"Authorization": f"Bearer {self.config.api_key}"}
        params = {}
        
        if fields:
            params["fields"] = ",".join(fields)
        
        # Execute request with retry logic
        for attempt in range(self.config.max_retries + 1):
            try:
                self.logger.debug(f"API request attempt {attempt + 1}/{self.config.max_retries + 1}")
                
                response = requests.get(
                    url,
                    headers=headers,
                    params=params,
                    timeout=self.config.timeout
                )
                response.raise_for_status()
                
                return {
                    "status": "success",
                    "resource_id": resource_id,
                    "data": response.json()
                }
            
            except requests.RequestException as e:
                self.logger.warning(f"Request failed (attempt {attempt + 1}): {e}")
                
                if attempt < self.config.max_retries:
                    await asyncio.sleep(2 ** attempt)  # Exponential backoff
                else:
                    raise
        
        raise RuntimeError("Max retries exceeded")
    
    # -------------------------------------------------------------------------
    # Utility Methods
    # -------------------------------------------------------------------------
    
    def get_state(self) -> Dict[str, Any]:
        """
        Get current plugin state
        
        Returns:
            Plugin state dictionary
        """
        return {
            "config": self.config.dict(),
            "state": {
                **self.state,
                "cache": f"{len(self.state.get('cache', {}))} entries" if self.state.get("cache") else "disabled"
            },
            "sessions": len(self.session_data)
        }
    
    def clear_cache(self) -> None:
        """Clear the plugin cache"""
        if self.state.get("cache"):
            self.state["cache"].clear()
            self.logger.info("Cache cleared")
    
    def get_metrics(self) -> Dict[str, Any]:
        """
        Get plugin metrics
        
        Returns:
            Metrics dictionary
        """
        return {
            "execution_count": self.state["execution_count"],
            "error_count": len(self.state["errors"]),
            "cache_size": len(self.state.get("cache", {})),
            "active_sessions": len(self.session_data),
            "uptime": (
                datetime.utcnow() - 
                datetime.fromisoformat(self.state["initialized_at"])
            ).total_seconds()
        }


# Export plugin class
__all__ = ["ExamplePlugin"]
