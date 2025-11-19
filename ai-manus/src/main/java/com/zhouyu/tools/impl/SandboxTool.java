package com.zhouyu.tools.impl;

import com.zhouyu.tools.BaseTool;
import com.zhouyu.tools.ToolResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class SandboxTool extends BaseTool {
    private static final Logger logger = LoggerFactory.getLogger(SandboxTool.class);
    private DockerSandbox sandbox;

    public SandboxTool() {
        super("sandbox", "Execute commands safely in a Docker container sandbox");
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return buildSchema(
                Map.of(
                        "action", enumParam("Sandbox action", List.of("start", "stop", "execute", "status")),
                        "command", stringParam("Command to execute in sandbox (required for execute)"),
                        "language", enumParam("Programming language for code execution", List.of("python", "bash", "node", "java")),
                        "code", stringParam("Code to execute (alternative to command)"),
                        "working_dir", stringParam("Working directory in container")
                ),
                List.of("action")
        );
    }

    @Override
    public ToolResult execute(Map<String, Object> parameters) {
        try {
            String action = getString(parameters, "action");

            logger.info("Executing sandbox action: {}", action);

            switch (action.toLowerCase()) {
                case "start":
                    return handleStart();
                case "stop":
                    return handleStop();
                case "execute":
                    return handleExecute(parameters);
                case "status":
                    return handleStatus();
                default:
                    return ToolResult.error("Unknown action: " + action);
            }
        } catch (Exception e) {
            logger.error("Sandbox operation failed", e);
            return ToolResult.error("Sandbox operation failed: " + e.getMessage());
        }
    }

    private ToolResult handleStart() {
        try {
            if (sandbox == null) {
                sandbox = new DockerSandbox();
            }

            if (sandbox.isRunning()) {
                return ToolResult.success("Sandbox is already running");
            }

            sandbox.start();
            return ToolResult.success("Sandbox started successfully");
        } catch (Exception e) {
            return ToolResult.error("Failed to start sandbox: " + e.getMessage());
        }
    }

    private ToolResult handleStop() {
        try {
            if (sandbox == null || !sandbox.isRunning()) {
                return ToolResult.success("Sandbox is not running");
            }

            sandbox.stop();
            return ToolResult.success("Sandbox stopped successfully");
        } catch (Exception e) {
            return ToolResult.error("Failed to stop sandbox: " + e.getMessage());
        }
    }

    private ToolResult handleExecute(Map<String, Object> parameters) {
        try {
            // Ensure sandbox is running
            if (sandbox == null) {
                sandbox = new DockerSandbox();
                sandbox.start();
            } else if (!sandbox.isRunning()) {
                sandbox.start();
            }

            String command = getString(parameters, "command");
            String code = getString(parameters, "code");
            String language = getString(parameters, "language");
            String workingDir = getString(parameters, "working_dir");

            // Build the actual command to execute
            String actualCommand;
            if (command != null) {
                actualCommand = command;
            } else if (code != null && language != null) {
                actualCommand = buildCodeExecutionCommand(code, language);
            } else {
                return ToolResult.error("Either 'command' or both 'code' and 'language' must be provided");
            }

            // Add working directory change if specified
            if (workingDir != null) {
                actualCommand = "cd " + workingDir + " && " + actualCommand;
            }

            logger.info("Executing in sandbox: {}", actualCommand);

            DockerSandbox.SandboxExecutionResult result = sandbox.executeCommand(actualCommand);

            if (result.isSuccess()) {
                return ToolResult.success(result.getCombinedOutput());
            } else if (result.isTimedOut()) {
                return ToolResult.error("Command timed out");
            } else {
                return ToolResult.error("Command failed with exit code " + result.getExitCode() +
                        ":\n" + result.getCombinedOutput());
            }
        } catch (Exception e) {
            return ToolResult.error("Command execution failed: " + e.getMessage());
        }
    }

    private ToolResult handleStatus() {
        if (sandbox == null) {
            return ToolResult.success("Sandbox not initialized");
        }

        String status = sandbox.isRunning() ? "running" : "stopped";
        return ToolResult.success("Sandbox status: " + status);
    }

    public static String escapeForPythonCommand(String pythonCode) {
        // 先转义Python字符串中的特殊字符
        String escaped = pythonCode.replace("'", "'\"'\"'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");

        return "'" + escaped + "'";
    }

    private String buildCodeExecutionCommand(String code, String language) {
        switch (language.toLowerCase()) {
            case "python":
                // Use heredoc approach for better handling of complex code
                return buildHeredocCommand(code, "python3");
            case "bash":
                return code; // Execute bash directly
            case "node":
                // Use heredoc approach for better handling of complex code
                return buildHeredocCommand(code, "node");
            case "java":
                // For Java, create temp file using heredoc
                String javaHeredoc = buildHeredocToFile(code, "/tmp/Main.java");
                return javaHeredoc + " && cd /tmp && javac Main.java && java Main";
            default:
                throw new IllegalArgumentException("Unsupported language: " + language);
        }
    }

    private String buildHeredocCommand(String code, String interpreter) {
        String delimiter = generateHeredocDelimiter();
        return String.format("%s << '%s'\n%s\n%s", interpreter, delimiter, code, delimiter);
    }

    private String generateHeredocDelimiter() {
        return "OPENMANUS_CODE_EOF_" + System.currentTimeMillis();
    }

    private String buildHeredocToFile(String code, String filePath) {
        String delimiter = generateHeredocDelimiter();
        return String.format("cat > %s << '%s'\n%s\n%s", filePath, delimiter, code, delimiter);
    }

    /**
     * Cleanup method called when tool is no longer needed
     */
    public void cleanup() {
        if (sandbox != null && sandbox.isRunning()) {
            try {
                sandbox.stop();
            } catch (Exception e) {
                logger.error("Error during sandbox cleanup", e);
            }
        }
    }
}
