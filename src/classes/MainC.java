package classes;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;

public class MainC {
    // Variables (UI elements)
    public TextField gay_id;
    @FXML
    private Button adder;
    @FXML
    private Button confirm;
    @FXML
    private ListView<Object> preview;
    @FXML
    private TextField product_name = new TextField();
    @FXML
    private TextField unit_price = new TextField();
    @FXML
    private TextField quantity = new TextField();
    @FXML
    private TextField total = new TextField();
    @FXML
    private ChoiceBox<String> choice;
    Product[] inventory;

    // Methods

    /**
     * Starter method.
     * Creates folders, adds listeners, and changes default properties where needed
     */
    public void initialize(){
        // init
        System.out.println("CaisseApp Launched Successfully!");
        new File(".\\tickets").mkdirs();

        // The following TextFields only accept numbers
        gay_id.textProperty().addListener(new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    unit_price.setText(newValue.replaceAll("\\D", ""));
                }
            }
        });
        unit_price.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    unit_price.setText(newValue.replaceAll("\\D", ""));
                }
            }
        });
        quantity.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    quantity.setText(newValue.replaceAll("\\D", ""));
                }
            }
        });

        // Ignore the ListView while traversing the app with Tab
        preview.setFocusTraversable(false);
    }
    /**
     * This method takes the info entered, and adds it as an element in the preview
     * Along with it, it adds a button to delete it from the preview if needed
     * Afterward, it cleans the UI by emptying the relevant TextFields, and sets focus back to data entry
     */
    @FXML
    protected void addToReceipt() {
        if (!Objects.equals(product_name.getText(), "") && !Objects.equals(total.getText(), "")) {
            Button big = new Button("Delete");
            big.setOnAction(new EventHandler<ActionEvent>()
            {
                public void handle(ActionEvent e)
                {
                   preview.getItems().remove(preview.getItems().indexOf(big)-1);
                   preview.getItems().remove(big);

                }
            });
            preview.getItems().add(String.join("-", product_name.getText().split(" ")) + "\n" + Integer.parseInt(quantity.getText()) + " " + Integer.parseInt(unit_price.getText()) + " " + (Integer.parseInt(quantity.getText()) * Integer.parseInt(unit_price.getText())));
            preview.getItems().add(big);
            // Reset view
            {
                product_name.setText("");
                unit_price.setText("");
                quantity.setText("");
                total.setText("");
            }
        }
        // Go back to entering information
        product_name.requestFocus();
    }

    /**
     * This method helps with navigating the app
     * It gives to each entry point a follow-up, so that the process can be smoothed
     * @param e defines an ActionEvent which is needed in order to trace which element called the method
     */
    @FXML
    private void next(ActionEvent e){
        String fid = ((TextField) e.getSource()).getId();
        switch (fid) {
            case "product_name" -> unit_price.requestFocus();
            case "unit_price" -> quantity.requestFocus();
            case "quantity" -> adder.requestFocus();
            default -> {
                product_name.requestFocus();
            }
        }
    }

    /**
     * This method exists for a somewhat convenient UX
     * It calculates the total for a given product on the fly, so the Total entry doesn't need to be manual
     */
    @FXML
    private void setTotal(){
        if(!Objects.equals(unit_price.getText(), "") && !Objects.equals(quantity.getText(), "")){
            int a = Integer.parseInt(unit_price.getText());
            int b = Integer.parseInt(quantity.getText());
            int c = a * b;
            total.setText(String.valueOf(c));
        } else total.setText("");
    }

    /**
     * This method helps with navigating the app
     * It sets the focus on the printing button when entering Shift+Enter
     * @param e defines a KeyEvent which is needed in order to trace which element called the method
     */
    @FXML
    private void go_to_print(KeyEvent e){
        if ((e.isShiftDown() && e.getCode()== KeyCode.ENTER) || (e.isControlDown() && e.getCode()==KeyCode.ENTER)){
            confirm.requestFocus();
        }
    }

    /**
     * Pretty much this project's main piece
     * This method connects the app to the printer, but only for the duration of the printing itself
     * As such, it requires a bit of loading time at every call, but helps to clear exceptions should any arise
     * The dedicated printer is defined in here
     * For the context of this app, we use the POS-58 printer, but adding an option to add more printer would be good
     * The content of the ticket is also defined here, and could possibly use its own method
     * Finally, the call is made, and for development while the printer is unavailable,
     * we can just output the ticket to the console directly
     * The ticket is then saved, the UI cleaned, and focus back to data entry
     */
    @FXML
    private void print(){
        // Total spent on buy
        int total_buy = 0;

        // Initialize service and connect to printer
        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PAGEABLE;
        PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
        patts.add(Sides.DUPLEX);

        PrintService[] ps = PrintServiceLookup.lookupPrintServices(flavor, patts);
        if (ps.length == 0) {
            //  throw new IllegalStateException("No Printer Found");
            System.err.println("No Printer Found");
        }
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null,null);
        PrintService myService = null;
        for (PrintService p : printServices){

            //Get Printer Name
            // Uncomment next line to display available printers
            // System.out.println(p.getName());
            if(p.getName().equals("POS-58")){
                myService = p;
                break;
            }
        }
        if (myService == null){
            throw new IllegalStateException("Printer Not Found!");
        }

        // Define message
        ByteArrayOutputStream message = new ByteArrayOutputStream();

        // Add Header
        message.writeBytes("------------------------------\n".getBytes());
        message.writeBytes(String.format("%-7s %s %7s%n", "|", "HAROUNA MAIGA", "|").getBytes());
        message.writeBytes(String.format("%-6s %s %6s%n", "|", "TEL: 0103492233", "|").getBytes());
        message.writeBytes(String.format("%-6s %s %6s%n", "|", "TEL: 0170988623", "|").getBytes());
        message.writeBytes(String.format("%-7s %s %6s%n", "|", "VENTE EN GROS,", "|").getBytes());
        message.writeBytes(String.format("%-4s %s %4s%n", "|", "SEMI GROS, DETAILS ", "|").getBytes());
        message.writeBytes("------------------------------\n".getBytes());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String time = dtf.format(LocalDateTime.now());
        message.writeBytes(String.format("%-4s %s%n", "", time).getBytes());

        if (!gay_id.getText().equals("")){
            message.writeBytes(String.format("%-14s %s %n%n","      Client:", gay_id.getText()).getBytes());
        }

        // Add headboard
        String headboard = String.format("%-9s %3s %6s %7s%n%n%n", "Produit", "Qte", "Prix", "Total");
        message.writeBytes(headboard.getBytes());

        // Add the different products from list
        for (Object prod: preview.getItems()
             ) {
            if(prod.getClass().equals("".getClass())){
                String line = prod.toString().replace("\n", " ");
                String[] el = line.split(" ");
                String newline = String.format("%-9s %3d %6d %7d %n", el[0].replace("-", " "), Integer.parseInt(el[1]), Integer.parseInt(el[2]), Integer.parseInt(el[3]));
                message.writeBytes(newline.getBytes());
                total_buy += Integer.parseInt(el[3]);
            }
        }

        // Display Total
        message.writeBytes("------------------------------\n".getBytes());
        message.writeBytes(String.format("Total: %d%n", total_buy).getBytes());
        message.writeBytes("\n".getBytes());

        // Add footer
        message.writeBytes("------------------------------\n".getBytes());
        message.writeBytes("Vos Achats, chez HAROUNA!".getBytes());
        message.writeBytes("\n\n\n\n".getBytes());

        // Create Print Document
        DocPrintJob job = myService.createPrintJob();
        Doc doc = new SimpleDoc(message.toByteArray(), DocFlavor.BYTE_ARRAY.AUTOSENSE, null);

        // Launch Printer
        try {
            // Uncomment next line to print ticket to printer
            job.print(doc, new HashPrintRequestAttributeSet());

            // Uncomment next line to print ticket to console
            System.out.println(message);

            // Save ticket to local storage
            if(!save_ticket(message, time)){
                Alert alert = new Alert(Alert.AlertType.ERROR, "ERREUR! Le ticket n'a pas été enregistré!");
            }

            // Clean up UI
            preview.getItems().removeAll(preview.getItems());
            gay_id.setText("");
            throw new PrintException();
        } catch (PrintException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "ERREUR! Veuillez signaler cette erreur à votre développeur");
            alert.show();
        } finally {
            // Go back to entering information
            product_name.requestFocus();
        }
    }

    /**
     * This method records every printed ticket in local storage
     * It stores them in the 'tickets' folder, and sorts out the tickets with id into their dedicated folders
     * @param msg is the content of the ticket, ready to be printed again
     * @param date is the time at which the ticket was printed. This info is also recorded in the ticket itself
     * @return whether the save was successful
     */
    private boolean save_ticket(ByteArrayOutputStream msg, String date){
        try {
            File saved;
            if (gay_id.getText().equals("")){
                saved = new File("tickets\\" + date.replace(" ", "_").replace(":","-") + ".txt");
            }else{
                new File(".\\tickets\\" + gay_id.getText()).mkdirs();
                saved = new File("tickets\\" + gay_id.getText() + "\\" + date.replace(" ", "_").replace(":","-") + ".txt");
            }
            if (saved.createNewFile()){
                try (FileWriter fw = new FileWriter(saved)){
                    fw.write(msg.toString());
                    return true;
                } catch (IOException e){
                    return false;
                }
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    // TODO

    /**
     * This method is a standby version of the 'print' function
     * It serves in printing pre-recorded tickets
     */
    private void print_(){}
    @FXML
    private void createProduct(){
        if (product_name.getText().equals("")){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ajoutez le nom du produit");
            alert.show();
        }
        if(unit_price.getText().equals("")){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ajoutez le prix du produit");
            alert.show();
        }
        if (!product_name.getText().equals("") && !unit_price.getText().equals("")) {
            System.out.println("Writing");
            try{
                Files.write(Paths.get(".\\src\\resources\\inventaire.txt"), (product_name.getText() + " " + unit_price.getText() + "\n").getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                String cwd = Path.of("").toAbsolutePath().toString();
                System.out.println(cwd);
            }
        }
        //reload_inventory();
    }
    private void reload_inventory(){
        try {
            File inv = new File(".\\src\\resources\\inventaire.txt");
            int lines = 0;
            Scanner reader = new Scanner(inv);
            Scanner counter = new Scanner(inv);
            while (counter.hasNextLine()){
                counter.nextLine();
                lines ++;
            }
            inventory = new Product[lines];
            int curr = 0;
            while (reader.hasNextLine()){
                String data = reader.nextLine();
                String[] rel = data.split(" ");
                inventory[curr] = new Product(rel[0], Integer.parseInt(rel[1]));
                choice = new ChoiceBox<>();
                choice.getItems().add(inventory[curr].name);
                //System.out.println(ch.getItems().get(curr));
            }
        } catch (IOException e){
            String cwd = Path.of("").toAbsolutePath().toString();
            System.out.println(cwd);
        }
    }
}

class Product{
    int price;
    String name;
    public Product(String name, int price){
        this.name = name;
        this.price = price;
    }
}