package ma.enset.service;
import java.util.HashMap;
import java.util.Map;
import io.grpc.stub.StreamObserver;
import ma.enset.stubs.Chat;
import ma.enset.stubs.ChatServiceGrpc;

public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {

    private static int lastClientId = 0;
    private Map<Integer, StreamObserver<Chat.ChatResponse>> clients = new HashMap<>();

    @Override
    public StreamObserver<Chat.ChatRequest> chat(StreamObserver<Chat.ChatResponse> responseObserver) {
        int clientId = ++lastClientId;

        clients.put(clientId, responseObserver);
        Chat.ChatResponse welcomeMessage = Chat.ChatResponse.newBuilder()
                .build();
        responseObserver.onNext(welcomeMessage);

        return new StreamObserver<Chat.ChatRequest>() {
            @Override
            public void onNext(Chat.ChatRequest chatRequest) {
                String message = chatRequest.getContent();
                String sender = chatRequest.getUser();
                Chat.ChatResponse response = Chat.ChatResponse.newBuilder()
                        .setUser(sender)
                        .setContent(message)
                        .build();
                if(message.contains("=>")){
                    String[] arrOfStr = message.split("=>");
                    int id = Integer.parseInt(arrOfStr[0]);
                    String msg = arrOfStr[1];
                    sendMessageToClient(msg, id,sender);
                } else {
                    broadcast(response, clientId);
                }

                System.out.println(response+""+clientId);
            }

            @Override
            public void onError(Throwable throwable) {
                clients.remove(clientId);
                System.out.println(throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                clients.remove(clientId);
                responseObserver.onCompleted();
            }
        };
    }
    private void broadcast(Chat.ChatResponse response, int senderId) {
        for (Map.Entry<Integer, StreamObserver<Chat.ChatResponse>> entry : clients.entrySet()) {
            int clientId = entry.getKey();
            StreamObserver<Chat.ChatResponse> observer = entry.getValue();
            if (clientId != senderId) {
                observer.onNext(response);
            }
        }
    }

    private void sendMessageToClient(String message, int clientId, String sender) {
        for (Map.Entry<Integer, StreamObserver<Chat.ChatResponse>> entry : clients.entrySet()) {
            int id = entry.getKey();
            StreamObserver<Chat.ChatResponse> observer = entry.getValue();
            if (id == clientId) {
                Chat.ChatResponse response = Chat.ChatResponse.newBuilder()
                        .setUser(sender)
                        .setContent(message)
                        .build();
                observer.onNext(response);
            }
        }
    }
}
