module com.example.theknife {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;


    opens com.example.theknife to javafx.fxml;
    exports com.example.theknife;
}