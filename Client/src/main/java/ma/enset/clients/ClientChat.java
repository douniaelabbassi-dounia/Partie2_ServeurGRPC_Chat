package ma.enset.clients;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ma.enset.stubs.Chat;
import ma.enset.stubs.ChatServiceGrpc;
import java.util.Scanner;

public class ClientChat extends Application {
    ObservableList<String>observableList=FXCollections.observableArrayList();
            public static void main(String[] args) {
           launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 12345)
                .usePlaintext()
                .build();
        ChatServiceGrpc.ChatServiceStub asyncstub = ChatServiceGrpc.newStub(channel);

        BorderPane borderPane =new BorderPane();
        stage.setTitle("Client ");
        Label label=new Label("Name");
        label.setTextFill(Color.WHITE);
        Label label1=new Label("Text Message");
        label1.setTextFill(Color.WHITE);
        TextField textUser=new TextField();
        TextField textChat=new TextField();
        Button buttonSend=new Button("Envoyer");
        ListView<String> listView =new ListView<>(observableList);
        HBox hBox=new HBox(label,textUser,label1,textChat,buttonSend);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10));
        hBox.setBackground(new Background(new BackgroundFill(Color.PURPLE,null,null)));
        borderPane.setCenter(listView);
        borderPane.setTop(hBox);
        Scene scene=new Scene(borderPane);
        stage.setScene(scene);
        stage.show();

        StreamObserver<Chat.ChatRequest> df =
                asyncstub.chat(new StreamObserver<Chat.ChatResponse>() {
                    @Override
                    public void onNext(Chat.ChatResponse chatResponse) {
                       Platform.runLater(()->{
                           observableList.add(chatResponse.getUser());
                           observableList.add(chatResponse.getContent());
                       });


                    }

                    @Override
                    public void onError(Throwable throwable) {

                        System.out.println(throwable.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Fin");
                    }
                });
        buttonSend.setOnAction((actionEvent -> {
            String name=textUser.getText();
            String content=textChat.getText();
            Chat.ChatRequest request =Chat.ChatRequest.newBuilder()
                    .setUser(name)
                    .setContent(content)
                    .build();
            df.onNext(request);
            Platform.runLater(()->{
                textUser.setDisable(true);
                textChat.setText("");
                observableList.add(content);
            });

        }));
    }
}


