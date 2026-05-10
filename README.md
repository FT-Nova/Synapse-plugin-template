# SYNAPSE Plugin Template

🚀 **Use this template** to create your own SYNAPSE plugin!

This repository provides a complete, production-ready starting point for developing SYNAPSE plugins.

[![Use this template](https://img.shields.io/badge/use%20this-template-blue?logo=github)](https://github.com/FTMahringer/Synapse-Plugin-Template/generate)

---

## 📋 What's Included

- ✅ **Complete plugin structure** with example implementation
- ✅ **Configuration examples** for common use cases
- ✅ **Testing setup** with example tests
- ✅ **CI/CD workflows** for validation and testing
- ✅ **Documentation templates** for your plugin
- ✅ **Development tools** and utilities

---

## 🎯 Quick Start

### 1. Use This Template

Click the **"Use this template"** button above to create your own repository.

### 2. Clone Your Repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_PLUGIN_NAME.git
cd YOUR_PLUGIN_NAME
```

### 3. Customize Your Plugin

Update the following files:

- `plugin.yaml` - Plugin metadata and configuration
- `src/` - Your plugin implementation
- `README.md` - This file! Make it yours.
- `LICENSE` - Choose your license

### 4. Develop Your Plugin

```bash
# Install dependencies (Python example)
pip install -r requirements.txt

# Run tests
pytest tests/

# Validate plugin
synapse plugin validate .
```

### 5. Test Locally

```bash
# Link plugin for local development
synapse plugin link .

# Test with SYNAPSE
synapse dev --watch-plugins .
```

---

## 📦 Plugin Structure

```
synapse-plugin-template/
├── README.md                 # This file
├── LICENSE                   # Plugin license
├── plugin.yaml               # Plugin manifest
├── .gitignore
│
├── src/                      # Plugin source code
│   ├── __init__.py
│   └── example_plugin.py     # Example implementation
│
├── tests/                    # Plugin tests
│   ├── __init__.py
│   └── test_example.py
│
├── docs/                     # Plugin documentation
│   ├── usage.md
│   └── configuration.md
│
├── examples/                 # Usage examples
│   └── basic_example.yaml
│
├── .github/
│   └── workflows/
│       ├── test.yml          # CI testing
│       └── validate.yml      # Plugin validation
│
└── requirements.txt          # Python dependencies
```

---

## 🔧 Plugin Development

### Plugin Manifest (plugin.yaml)

The `plugin.yaml` file defines your plugin's metadata:

```yaml
name: my-plugin
version: 1.0.0
description: Short description of what your plugin does

author: Your Name
maintainers:
  - name: Your Name
    email: your.email@example.com
    github: yourusername

synapse_version: ">=2.0.0,<3.0.0"

tools:
  - name: example_tool
    description: Example tool description
    parameters:
      - name: input
        type: string
        required: true
        description: Input parameter description
```

### Implementing Tools

Example plugin implementation (Python):

```python
from synapse.plugin import Plugin, tool

class MyPlugin(Plugin):
    """My awesome SYNAPSE plugin."""
    
    @tool(
        name="example_tool",
        description="Does something useful"
    )
    def example_tool(self, input: str) -> dict:
        """
        Example tool implementation.
        
        Args:
            input: User input
            
        Returns:
            Result dictionary
        """
        return {
            "status": "success",
            "result": f"Processed: {input}"
        }
```

### Plugin Lifecycle

Your plugin can implement lifecycle hooks:

```python
class MyPlugin(Plugin):
    def on_load(self):
        """Called when plugin is loaded."""
        print("Plugin loaded!")
    
    def on_enable(self):
        """Called when plugin is enabled."""
        self.init_resources()
    
    def on_disable(self):
        """Called when plugin is disabled."""
        self.cleanup_resources()
```

---

## 🧪 Testing Your Plugin

### Unit Tests

Write tests in the `tests/` directory:

```python
import pytest
from src.example_plugin import MyPlugin

def test_example_tool():
    plugin = MyPlugin()
    result = plugin.example_tool("test input")
    
    assert result["status"] == "success"
    assert "test input" in result["result"]
```

### Run Tests

```bash
# Run all tests
pytest tests/

# Run with coverage
pytest --cov=src tests/

# Run specific test
pytest tests/test_example.py::test_example_tool
```

---

## 📝 Configuration

### Plugin Configuration

Users can configure your plugin in their SYNAPSE config:

```yaml
plugins:
  my-plugin:
    api_key: ${MY_PLUGIN_API_KEY}
    timeout_seconds: 30
    enable_caching: true
```

### Environment Variables

Use environment variables for sensitive data:

```bash
export MY_PLUGIN_API_KEY=your_api_key_here
```

### Configuration Schema

Define configuration schema in `plugin.yaml`:

```yaml
config_schema:
  api_key:
    type: string
    required: true
    description: API key for the service
  
  timeout_seconds:
    type: integer
    default: 30
    description: Request timeout in seconds
```

---

## 🚀 Publishing Your Plugin

### 1. Test Thoroughly

```bash
# Run all tests
pytest tests/

# Validate plugin
synapse plugin validate .

# Test in SYNAPSE
synapse dev --watch-plugins .
```

### 2. Update Documentation

- Complete README.md
- Add usage examples
- Document all tools and parameters
- Add troubleshooting section

### 3. Choose Publishing Method

#### Option A: Submit to Community Repository

Submit your plugin to the official community repository:

1. Fork [Synapse-Plugins-Community](https://github.com/FTMahringer/Synapse-Plugins-Community)
2. Add your plugin to the `plugins/` directory
3. Submit a Pull Request
4. Wait for review

See the [Community Contribution Guide](https://github.com/FTMahringer/Synapse-Plugins-Community/blob/main/CONTRIBUTING.md) for details.

#### Option B: Distribute Independently

Keep your plugin in your own repository:

```bash
# Users install directly from your repo
synapse plugin install https://github.com/YOUR_USERNAME/YOUR_PLUGIN_NAME
```

---

## 🔒 Security Best Practices

### Permissions

Declare required permissions in `plugin.yaml`:

```yaml
permissions:
  - network.http            # HTTP/HTTPS requests
  - filesystem.read         # Read files
  - filesystem.write:/tmp   # Write to /tmp only
  - process.spawn           # Create subprocesses
```

### Input Validation

Always validate user input:

```python
@tool(name="safe_tool")
def safe_tool(self, user_input: str) -> dict:
    # Validate input
    if not user_input or len(user_input) > 1000:
        return {"status": "error", "message": "Invalid input"}
    
    # Sanitize input
    sanitized = self.sanitize_input(user_input)
    
    # Process safely
    return self.process(sanitized)
```

### Resource Limits

Set resource limits in `plugin.yaml`:

```yaml
limits:
  memory_mb: 256
  cpu_percent: 50
  timeout_seconds: 30
```

---

## 📚 Resources

### Documentation

- [SYNAPSE Documentation](https://ftmahringer.github.io/Synapse/)
- [Plugin Development Guide](https://ftmahringer.github.io/Synapse/plugins/development/getting-started)
- [Plugin API Reference](https://ftmahringer.github.io/Synapse/plugins/architecture)

### Examples

- [Official Plugins](https://github.com/FTMahringer/Synapse-Plugins)
- [Community Plugins](https://github.com/FTMahringer/Synapse-Plugins-Community)

### Support

- [GitHub Discussions](https://github.com/FTMahringer/Synapse-Plugin-Template/discussions)
- [Report Issues](https://github.com/FTMahringer/Synapse-Plugin-Template/issues)

---

## 🤝 Contributing

Contributions to improve this template are welcome!

1. Fork this repository
2. Create a feature branch
3. Make your changes
4. Submit a Pull Request

---

## 📄 License

This template is licensed under the MIT License - see [LICENSE](LICENSE) for details.

Your plugin can use any license you choose. Common choices:

- **MIT** - Permissive, allows commercial use
- **Apache 2.0** - Permissive with patent grant
- **GPL-3.0** - Copyleft, requires derivatives to be open source

---

## 🎉 Next Steps

Now that you've created your plugin repository:

1. ✅ Customize `plugin.yaml` with your plugin details
2. ✅ Implement your plugin tools in `src/`
3. ✅ Write tests in `tests/`
4. ✅ Update this README with your plugin documentation
5. ✅ Add usage examples in `examples/`
6. ✅ Test locally with SYNAPSE
7. ✅ Submit to Community Repository or distribute independently

**Happy plugin development!** 🚀
