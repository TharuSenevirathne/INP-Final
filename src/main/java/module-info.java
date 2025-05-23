module org.example.finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens org.example.finalproject to javafx.fxml;
    exports org.example.finalproject;
}