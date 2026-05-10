"""
Comprehensive test suite for Example Plugin

Tests cover:
- Plugin initialization and configuration
- Tool execution
- Lifecycle hooks
- State management
- Error handling
- Caching behavior
- Async operations
"""

import asyncio
import pytest
from datetime import datetime
from typing import Dict, Any
from unittest.mock import Mock, patch, AsyncMock

import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..'))

from src.example_plugin import ExamplePlugin, PluginConfig


class TestPluginConfig:
    """Test configuration validation and defaults"""
    
    def test_default_config(self):
        """Test default configuration values"""
        config = PluginConfig()
        
        assert config.api_key == ""
        assert config.endpoint == "https://api.example.com"
        assert config.timeout == 30
        assert config.max_retries == 3
        assert config.enable_caching is True
        assert config.log_level == "INFO"
    
    def test_custom_config(self):
        """Test custom configuration"""
        config = PluginConfig(
            api_key="test-key-123",
            endpoint="https://custom.api.com",
            timeout=60,
            max_retries=5,
            enable_caching=False,
            log_level="DEBUG"
        )
        
        assert config.api_key == "test-key-123"
        assert config.endpoint == "https://custom.api.com"
        assert config.timeout == 60
        assert config.max_retries == 5
        assert config.enable_caching is False
        assert config.log_level == "DEBUG"
    
    def test_log_level_validation(self):
        """Test log level validation"""
        valid_levels = ["DEBUG", "INFO", "WARNING", "ERROR", "CRITICAL"]
        
        for level in valid_levels:
            config = PluginConfig(log_level=level.lower())
            assert config.log_level == level.upper()
        
        with pytest.raises(ValueError, match="log_level must be one of"):
            PluginConfig(log_level="INVALID")
    
    def test_endpoint_validation(self):
        """Test endpoint URL validation"""
        with pytest.raises(ValueError, match="endpoint must start with"):
            PluginConfig(endpoint="ftp://invalid.com")
        
        PluginConfig(endpoint="http://valid.com")
        PluginConfig(endpoint="https://valid.com")
    
    def test_timeout_bounds(self):
        """Test timeout boundary validation"""
        with pytest.raises(ValueError):
            PluginConfig(timeout=0)
        
        with pytest.raises(ValueError):
            PluginConfig(timeout=301)
        
        PluginConfig(timeout=1)
        PluginConfig(timeout=300)
    
    def test_max_retries_bounds(self):
        """Test max_retries boundary validation"""
        with pytest.raises(ValueError):
            PluginConfig(max_retries=-1)
        
        with pytest.raises(ValueError):
            PluginConfig(max_retries=11)
        
        PluginConfig(max_retries=0)
        PluginConfig(max_retries=10)


class TestPluginInitialization:
    """Test plugin initialization"""
    
    def test_init_with_no_config(self):
        """Test initialization without configuration"""
        plugin = ExamplePlugin()
        
        assert plugin.config is not None
        assert plugin.logger is not None
        assert plugin.state["execution_count"] == 0
        assert isinstance(plugin.state["cache"], dict)
        assert plugin.state["errors"] == []
        assert "initialized_at" in plugin.state
    
    def test_init_with_config(self):
        """Test initialization with configuration"""
        config = {
            "api_key": "test-key",
            "timeout": 60,
            "log_level": "DEBUG"
        }
        
        plugin = ExamplePlugin(config)
        
        assert plugin.config.api_key == "test-key"
        assert plugin.config.timeout == 60
        assert plugin.config.log_level == "DEBUG"
    
    def test_cache_disabled(self):
        """Test initialization with caching disabled"""
        plugin = ExamplePlugin({"enable_caching": False})
        
        assert plugin.state["cache"] is None


@pytest.mark.asyncio
class TestLifecycleHooks:
    """Test plugin lifecycle hooks"""
    
    async def test_on_startup(self):
        """Test startup hook"""
        plugin = ExamplePlugin({"api_key": "test-key"})
        
        await plugin.on_startup()
        
        # Should complete without errors
        assert plugin.logger is not None
    
    async def test_on_shutdown(self):
        """Test shutdown hook"""
        plugin = ExamplePlugin()
        plugin.state["cache"]["test"] = "value"
        plugin.state["execution_count"] = 10
        
        await plugin.on_shutdown()
        
        # Cache should be cleared
        assert len(plugin.state["cache"]) == 0
    
    async def test_on_session_start(self):
        """Test session start hook"""
        plugin = ExamplePlugin()
        session_id = "test-session-123"
        context = {"user": "test_user", "workspace": "/test"}
        
        await plugin.on_session_start(session_id, context)
        
        assert session_id in plugin.session_data
        assert plugin.session_data[session_id]["context"] == context
        assert "started_at" in plugin.session_data[session_id]
        assert plugin.session_data[session_id]["tool_calls"] == []
    
    async def test_on_session_end(self):
        """Test session end hook"""
        plugin = ExamplePlugin()
        session_id = "test-session-123"
        
        await plugin.on_session_start(session_id, {})
        assert session_id in plugin.session_data
        
        await plugin.on_session_end(session_id)
        assert session_id not in plugin.session_data
    
    async def test_on_tool_execute(self):
        """Test tool execution hook"""
        plugin = ExamplePlugin()
        session_id = "test-session-123"
        
        await plugin.on_session_start(session_id, {})
        await plugin.on_tool_execute(
            "example_tool",
            {"query": "test"},
            session_id
        )
        
        tool_calls = plugin.session_data[session_id]["tool_calls"]
        assert len(tool_calls) == 1
        assert tool_calls[0]["tool"] == "example_tool"
        assert tool_calls[0]["parameters"] == {"query": "test"}
    
    async def test_on_error(self):
        """Test error hook"""
        plugin = ExamplePlugin()
        error = ValueError("Test error")
        context = {"operation": "test_operation"}
        
        await plugin.on_error(error, context)
        
        assert len(plugin.state["errors"]) == 1
        error_info = plugin.state["errors"][0]
        assert error_info["error"] == "Test error"
        assert error_info["type"] == "ValueError"
        assert error_info["context"] == context


@pytest.mark.asyncio
class TestExampleTool:
    """Test example_tool functionality"""
    
    async def test_basic_execution(self):
        """Test basic tool execution"""
        plugin = ExamplePlugin()
        
        result = await plugin.example_tool("test query")
        
        assert result["status"] == "success"
        assert result["query"] == "test query"
        assert result["format"] == "json"
        assert "timestamp" in result
        assert plugin.state["execution_count"] == 1
    
    async def test_text_format(self):
        """Test text format output"""
        plugin = ExamplePlugin()
        
        result = await plugin.example_tool(
            "hello world",
            {"format": "text"}
        )
        
        assert result["processed"] == "HELLO WORLD"
        assert result["format"] == "text"
    
    async def test_json_format(self):
        """Test JSON format output"""
        plugin = ExamplePlugin()
        
        result = await plugin.example_tool(
            "hello world",
            {"format": "json"}
        )
        
        assert isinstance(result["processed"], dict)
        assert result["processed"]["original"] == "hello world"
        assert result["processed"]["length"] == 11
        assert result["processed"]["words"] == 2
    
    async def test_verbose_output(self):
        """Test verbose output"""
        plugin = ExamplePlugin()
        
        result = await plugin.example_tool(
            "test",
            {"verbose": True}
        )
        
        assert "metadata" in result
        assert "execution_count" in result["metadata"]
        assert "plugin_version" in result["metadata"]
    
    async def test_caching(self):
        """Test caching behavior"""
        plugin = ExamplePlugin()
        
        # First call
        result1 = await plugin.example_tool("test query")
        timestamp1 = result1["timestamp"]
        
        # Wait a bit
        await asyncio.sleep(0.01)
        
        # Second call - should be cached
        result2 = await plugin.example_tool("test query")
        timestamp2 = result2["timestamp"]
        
        assert timestamp1 == timestamp2  # Same timestamp = cached
        assert plugin.state["execution_count"] == 2  # Still increments
    
    async def test_cache_disabled(self):
        """Test behavior with caching disabled"""
        plugin = ExamplePlugin({"enable_caching": False})
        
        result1 = await plugin.example_tool("test")
        await asyncio.sleep(0.01)
        result2 = await plugin.example_tool("test")
        
        # Different timestamps = not cached
        assert result1["timestamp"] != result2["timestamp"]


@pytest.mark.asyncio
class TestFetchData:
    """Test fetch_data functionality"""
    
    async def test_no_api_key_mock_data(self):
        """Test mock data when no API key configured"""
        plugin = ExamplePlugin()
        
        result = await plugin.fetch_data("resource-123")
        
        assert result["status"] == "mock"
        assert result["resource_id"] == "resource-123"
        assert "data" in result
    
    async def test_with_fields(self):
        """Test field filtering"""
        plugin = ExamplePlugin()
        
        result = await plugin.fetch_data(
            "resource-123",
            fields=["name", "email", "status"]
        )
        
        assert result["fields"] == ["name", "email", "status"]
    
    @patch('requests.get')
    async def test_successful_api_call(self, mock_get):
        """Test successful API call"""
        mock_response = Mock()
        mock_response.json.return_value = {"id": "123", "name": "Test"}
        mock_response.raise_for_status = Mock()
        mock_get.return_value = mock_response
        
        plugin = ExamplePlugin({"api_key": "test-key"})
        
        result = await plugin.fetch_data("resource-123")
        
        assert result["status"] == "success"
        assert result["data"]["id"] == "123"
        assert result["data"]["name"] == "Test"
        
        # Verify API call
        mock_get.assert_called_once()
        call_kwargs = mock_get.call_args[1]
        assert "Bearer test-key" in call_kwargs["headers"]["Authorization"]
    
    @patch('requests.get')
    async def test_retry_logic(self, mock_get):
        """Test retry logic on failure"""
        mock_get.side_effect = [
            Exception("Connection error"),
            Exception("Connection error"),
            Mock(json=lambda: {"success": True}, raise_for_status=Mock())
        ]
        
        plugin = ExamplePlugin({
            "api_key": "test-key",
            "max_retries": 2
        })
        
        result = await plugin.fetch_data("resource-123")
        
        assert result["status"] == "success"
        assert mock_get.call_count == 3  # Initial + 2 retries
    
    @patch('requests.get')
    async def test_max_retries_exceeded(self, mock_get):
        """Test behavior when max retries exceeded"""
        mock_get.side_effect = Exception("Connection error")
        
        plugin = ExamplePlugin({
            "api_key": "test-key",
            "max_retries": 2
        })
        
        with pytest.raises(Exception):
            await plugin.fetch_data("resource-123")
        
        assert mock_get.call_count == 3  # Initial + 2 retries


class TestUtilityMethods:
    """Test utility methods"""
    
    def test_get_state(self):
        """Test get_state method"""
        plugin = ExamplePlugin({"api_key": "test-key"})
        plugin.state["execution_count"] = 5
        
        state = plugin.get_state()
        
        assert "config" in state
        assert "state" in state
        assert "sessions" in state
        assert state["state"]["execution_count"] == 5
    
    def test_clear_cache(self):
        """Test clear_cache method"""
        plugin = ExamplePlugin()
        plugin.state["cache"]["key1"] = "value1"
        plugin.state["cache"]["key2"] = "value2"
        
        plugin.clear_cache()
        
        assert len(plugin.state["cache"]) == 0
    
    def test_clear_cache_disabled(self):
        """Test clear_cache when caching disabled"""
        plugin = ExamplePlugin({"enable_caching": False})
        
        # Should not raise error
        plugin.clear_cache()
    
    def test_get_metrics(self):
        """Test get_metrics method"""
        plugin = ExamplePlugin()
        plugin.state["execution_count"] = 10
        plugin.state["cache"]["key"] = "value"
        
        metrics = plugin.get_metrics()
        
        assert metrics["execution_count"] == 10
        assert metrics["error_count"] == 0
        assert metrics["cache_size"] == 1
        assert metrics["active_sessions"] == 0
        assert "uptime" in metrics
        assert metrics["uptime"] >= 0


@pytest.mark.asyncio
class TestIntegration:
    """Integration tests"""
    
    async def test_full_lifecycle(self):
        """Test complete plugin lifecycle"""
        plugin = ExamplePlugin({
            "api_key": "test-key",
            "log_level": "DEBUG"
        })
        
        # Startup
        await plugin.on_startup()
        
        # Start session
        session_id = "integration-test"
        await plugin.on_session_start(session_id, {"test": True})
        
        # Execute tools
        await plugin.on_tool_execute("example_tool", {"query": "test"}, session_id)
        result1 = await plugin.example_tool("integration test")
        
        await plugin.on_tool_execute("fetch_data", {"resource_id": "123"}, session_id)
        result2 = await plugin.fetch_data("123")
        
        # Check metrics
        metrics = plugin.get_metrics()
        assert metrics["execution_count"] == 2
        assert metrics["active_sessions"] == 1
        
        # End session
        await plugin.on_session_end(session_id)
        
        # Shutdown
        await plugin.on_shutdown()
        
        # Verify final state
        final_state = plugin.get_state()
        assert final_state["sessions"] == 0
    
    async def test_concurrent_tool_execution(self):
        """Test concurrent tool execution"""
        plugin = ExamplePlugin()
        
        # Execute multiple tools concurrently
        tasks = [
            plugin.example_tool(f"query-{i}")
            for i in range(10)
        ]
        
        results = await asyncio.gather(*tasks)
        
        assert len(results) == 10
        assert all(r["status"] == "success" for r in results)
        assert plugin.state["execution_count"] == 10
    
    async def test_error_handling_integration(self):
        """Test error handling in integration scenario"""
        plugin = ExamplePlugin()
        
        # Simulate error
        error = RuntimeError("Test error")
        context = {"tool": "example_tool", "query": "test"}
        
        await plugin.on_error(error, context)
        
        # Plugin should continue working
        result = await plugin.example_tool("after error")
        assert result["status"] == "success"
        
        # Error should be recorded
        metrics = plugin.get_metrics()
        assert metrics["error_count"] == 1


if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short"])
