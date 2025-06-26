module com.example.theknife {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires com.opencsv;
    requires java.desktop;
    requires transitive javafx.graphics;


    opens com.example.theknife to javafx.fxml;
    exports com.example.theknife;
}