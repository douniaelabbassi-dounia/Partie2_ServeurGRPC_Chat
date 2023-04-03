package ma.enset.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import ma.enset.service.ChatServiceImpl;

public class ServeurGRPC {
    public static void main(String[] args) throws Exception {
        Server server= ServerBuilder.forPort(12345)
                .addService(new ChatServiceImpl())
                .build();
        server.start();
        System.out.println("Le serveur GRPC est démarreé........");
        server.awaitTermination();

    }
}

