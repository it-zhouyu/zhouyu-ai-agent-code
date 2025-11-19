package com.zhouyu;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.util.List;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public class DockerClientDemo {

    public static void main(String[] args) {
        try {
            // 先检查Docker服务是否可用
            Process process = Runtime.getRuntime().exec("docker version");
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Docker服务未运行，请启动Docker Desktop");
                return;
            }

            DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withDockerHost("unix:///var/run/docker.sock")
                    .build();

            System.out.println("Docker Host: " + config.getDockerHost());

            DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                    .dockerHost(config.getDockerHost())
                    .build();

            DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

            // 先测试ping
            dockerClient.pingCmd().exec();
            System.out.println("Docker连接测试成功");

            List<Image> images = dockerClient.listImagesCmd().exec();
            System.out.println("镜像数量: " + images.size());
            System.out.println(images);

        } catch (Exception e) {
            System.err.println("错误详情:");
            e.printStackTrace();

            // 提供具体的解决方案提示
            if (e.getMessage().contains("Connection refused") || e.getMessage().contains("No such file")) {
                System.err.println("\n=== 解决方案 ===");
                System.err.println("1. 请确保Docker Desktop正在运行");
                System.err.println("2. 检查Docker socket路径: ls -la /var/run/docker.sock");
                System.err.println("3. 尝试重启Docker Desktop");
            }
        }
    }
}
