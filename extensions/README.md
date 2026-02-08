# Writing Extensions for Pinora Browser

Drop compiled extension JARs into the `extensions/` directory at the root of the application.

Requirements:
- Your extension must implement the `com.pinora.browser.extensions.Extension` interface.
- Include a service provider file at `META-INF/services/com.pinora.browser.extensions.Extension` containing the implementation class name.

Example service file content:

```
com.example.pinora.MyExtension
```

At runtime the browser will create the `extensions/` directory if it does not exist. For local testing, build your extension JAR and copy it into that folder, then start the browser.
