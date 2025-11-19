package com.zhouyu.tools.impl;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class DockerSandbox {  // 沙箱

    private static final Logger logger = LoggerFactory.getLogger(DockerSandbox.class);

    private final DockerClient dockerClient;
    private String containerId;
    private boolean isRunning = false;

    public DockerSandbox() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("unix:///var/run/docker.sock")
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * Start the sandbox container
     */
    public void start() throws Exception {
        if (isRunning) {
            logger.warn("Sandbox is already running");
            return;
        }

        try {
            // Pull image if not exists
            pullImageIfNeeded();

            // Create container
            HostConfig hostConfig = new HostConfig()
                    .withMemory(parseMemoryLimit(SandboxSettings.memoryLimit))
                    .withCpuQuota((long) (SandboxSettings.cpuLimit * 100000))
                    .withCpuPeriod(100000L)
                    .withNetworkMode(SandboxSettings.networkEnabled ? "bridge" : "none")
                    .withAutoRemove(true);

            CreateContainerResponse container = dockerClient.createContainerCmd(SandboxSettings.image)
                    .withWorkingDir(SandboxSettings.workDir)
                    .withHostConfig(hostConfig)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withTty(true)
                    .exec();

            containerId = container.getId();

            // Start container
            dockerClient.startContainerCmd(containerId).exec();

            isRunning = true;
            logger.info("Sandbox started with container ID: {}", containerId);

        } catch (Exception e) {
            logger.error("Failed to start sandbox", e);
            throw new Exception("Sandbox startup failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute command in the sandbox
     */
    public SandboxExecutionResult executeCommand(String command) throws Exception {
        if (!isRunning) {
            throw new IllegalStateException("Sandbox is not running");
        }

        try {
            // Create exec instance
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withAttachStdout(true)
                    .withAttachStderr(true)
                    .withCmd("/bin/sh", "-c", command)
                    .exec();

            String execId = execCreateCmdResponse.getId();

            // Execute command
            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            ByteArrayOutputStream stderr = new ByteArrayOutputStream();

            ExecStartResultCallback callback = new ExecStartResultCallback(stdout, stderr);

            dockerClient.execStartCmd(execId).exec(callback);

            // Wait for completion with timeout
            boolean finished = callback.awaitCompletion(SandboxSettings.timeout, TimeUnit.SECONDS);

            if (!finished) {
                callback.close();
                return new SandboxExecutionResult(null, "Command timed out", 124, true);
            }

            // Get exit code
            InspectExecResponse execInfo = dockerClient.inspectExecCmd(execId).exec();
            Integer exitCode = execInfo.getExitCode();

            String stdoutStr = stdout.toString(StandardCharsets.UTF_8);
            String stderrStr = stderr.toString(StandardCharsets.UTF_8);

            return new SandboxExecutionResult(stdoutStr, stderrStr, exitCode != null ? exitCode : 0, false);

        } catch (Exception e) {
            logger.error("Command execution failed", e);
            throw new Exception("Command execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Copy file to sandbox
     */
    public void copyFileToSandbox(String localPath, String containerPath) throws Exception {
        if (!isRunning) {
            throw new IllegalStateException("Sandbox is not running");
        }

        try {
            // This is a simplified implementation
            // In practice, you'd use dockerClient.copyArchiveToContainerCmd()
            logger.info("Copying file from {} to {}", localPath, containerPath);
            // Implementation would go here
        } catch (Exception e) {
            logger.error("File copy failed", e);
            throw new Exception("File copy failed: " + e.getMessage(), e);
        }
    }

    /**
     * Copy file from sandbox
     */
    public void copyFileFromSandbox(String containerPath, String localPath) throws Exception {
        if (!isRunning) {
            throw new IllegalStateException("Sandbox is not running");
        }

        try {
            // This is a simplified implementation
            // In practice, you'd use dockerClient.copyArchiveFromContainerCmd()
            logger.info("Copying file from {} to {}", containerPath, localPath);
            // Implementation would go here
        } catch (Exception e) {
            logger.error("File copy failed", e);
            throw new Exception("File copy failed: " + e.getMessage(), e);
        }
    }

    /**
     * Stop and remove the sandbox
     */
    public void stop() {
        if (!isRunning) {
            return;
        }

        try {
            if (containerId != null) {
                dockerClient.stopContainerCmd(containerId).exec();
                logger.info("Sandbox stopped: {}", containerId);
            }
            isRunning = false;
            containerId = null;
        } catch (Exception e) {
            logger.error("Error stopping sandbox", e);
        }
    }

    /**
     * Check if sandbox is running
     */
    public boolean isRunning() {
        return isRunning;
    }

    private void pullImageIfNeeded() {
        try {
            List<Image> images = dockerClient.listImagesCmd()
                    .withImageNameFilter(SandboxSettings.image)
                    .exec();

            if (images.isEmpty()) {
                logger.info("Pulling Docker image: {}", SandboxSettings.image);
                dockerClient.pullImageCmd(SandboxSettings.image).start().awaitCompletion();
                logger.info("Image pulled successfully: {}", SandboxSettings.image);
            }
        } catch (Exception e) {
            logger.warn("Failed to check/pull image: {}", e.getMessage());
        }
    }

    private long parseMemoryLimit(String memoryLimit) {
        // Parse memory limit like "512m", "1g", etc.
        if (memoryLimit.endsWith("m") || memoryLimit.endsWith("M")) {
            return Long.parseLong(memoryLimit.substring(0, memoryLimit.length() - 1)) * 1024 * 1024;
        } else if (memoryLimit.endsWith("g") || memoryLimit.endsWith("G")) {
            return Long.parseLong(memoryLimit.substring(0, memoryLimit.length() - 1)) * 1024 * 1024 * 1024;
        } else {
            return Long.parseLong(memoryLimit);
        }
    }

    /**
     * Result of sandbox command execution
     */
    public static class SandboxExecutionResult {
        private final String stdout;
        private final String stderr;
        private final int exitCode;
        private final boolean timedOut;

        public SandboxExecutionResult(String stdout, String stderr, int exitCode, boolean timedOut) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
            this.timedOut = timedOut;
        }

        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
        public int getExitCode() { return exitCode; }
        public boolean isTimedOut() { return timedOut; }
        public boolean isSuccess() { return exitCode == 0 && !timedOut; }

        public String getCombinedOutput() {
            StringBuilder sb = new StringBuilder();
            if (stdout != null && !stdout.trim().isEmpty()) {
                sb.append(stdout);
            }
            if (stderr != null && !stderr.trim().isEmpty()) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("STDERR: ").append(stderr);
            }
            return sb.toString();
        }

        @Override
        public String toString() {
            return "SandboxExecutionResult{" +
                    "exitCode=" + exitCode +
                    ", timedOut=" + timedOut +
                    ", stdout='" + (stdout != null ? stdout.substring(0, Math.min(100, stdout.length())) : null) + "'" +
                    '}';
        }
    }

    public static class SandboxSettings {
        public static String image = "python:3.12-slim";
        public static String workDir = "/workspace";
        public static String memoryLimit = "512m";
        public static double cpuLimit = 1.0;
        public static int timeout = 300;
        public static boolean networkEnabled = false;
    }
}
