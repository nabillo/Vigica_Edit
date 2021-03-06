/*
 * Copyright (C) 2016 bnabi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vigica_edit.view;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.hibernate.HibernateException;
import vigica_edit.Decompress_mw_s1;
import vigica_edit.Generate_mw_s1;
import vigica_edit.Compare_mw_s1;
import vigica_edit.Service_BDD;
import vigica_edit.model.Service;

/**
 *
 * @author bnabi
 */
public class FXMLMainController implements Initializable {
        
    private Service_BDD bdd = new Service_BDD();
    static private Error_Msg error_msg = new Error_Msg();
    final FileChooser fileChooser = new FileChooser();
    static private String[] perf = {"GENERAL", "INFO", "DOCUMENTARY", "MOVIES", "TV SHOW", "ZIC", "SPORT", "KIDS", "DIN", "MISC"}; 
    
    /**
    * The data as an observable list of Service.
    */
    private ObservableList<Service> serviceData = FXCollections.observableArrayList();
    
    @FXML
    private Stage stage;
    @FXML
    private TableView<Service> serviceTable;
    @FXML
    private TableColumn<Service, Integer> s_idxColumn;
    @FXML
    private TableColumn<Service, String> s_nameColumn;
    @FXML
    private TableColumn<Service, String> s_typeColumn;
    @FXML
    private TableColumn<Service, Integer> s_nidColumn;
    @FXML
    private TableColumn<Service, String> s_pprColumn;
    @FXML
    private TableColumn<Service, String> s_newColumn;
    @FXML
    private TextField s_idx;
    @FXML
    private TextField s_type;
    @FXML
    private TextField s_name;
    @FXML
    private ProgressIndicator pi;
    @FXML
    private Label title;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public FXMLMainController() {
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pi.setVisible(false);
        serviceTable.setEditable(true);
        s_idxColumn.setCellValueFactory(cellData -> cellData.getValue().s_idxProperty().asObject());
        s_nameColumn.setCellValueFactory(cellData -> cellData.getValue().s_nameProperty());
        s_nameColumn.setEditable(true);
        s_typeColumn.setCellValueFactory(cellData -> cellData.getValue().s_typeProperty());
        s_nidColumn.setCellValueFactory(cellData -> cellData.getValue().s_nidProperty().asObject());
        s_pprColumn.setCellValueFactory(cellData -> cellData.getValue().s_pprProperty());
        s_newColumn.setCellValueFactory(cellData -> cellData.getValue().s_newProperty());
        
        // Context menu
        serviceTable.setRowFactory(new Callback<TableView<Service>, TableRow<Service>>() {
            @Override
            public TableRow<Service> call(TableView<Service> tableView) {
                final TableRow<Service> row = new TableRow<>();
                final ContextMenu rowMenu = new ContextMenu();

                final MenuItem removeItem = new MenuItem("Delete");
                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        final Service service = row.getItem();
                        serviceData.removeAll(service);
                        
                        try {
                            bdd.delete_bdd(service);
                        }
                        catch (HibernateException e) {
                            error_msg.Error_diag("Error delete service BDD\n"+e.getMessage());
                        }
                    }
                });
                
                rowMenu.getItems().addAll(removeItem);
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));
                return row;
            }
        });
    
        s_pprColumn.setCellFactory(new Callback<TableColumn<Service, String>, TableCell<Service, String>>() {
            @Override
            public TableCell<Service, String> call(TableColumn<Service, String> col) {
                final TableCell<Service, String> cell = new TableCell<>();
                
                cell.textProperty().bind(cell.itemProperty());
                cell.itemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                        if (newValue != null) {
                            final ContextMenu cellMenu = new ContextMenu();
                            for (int i=1; i<=10; i++) {
                                final CheckMenuItem prefMenuItem = new CheckMenuItem(perf[i-1]);
                                final int line = i;

                                prefMenuItem.setId(String.valueOf(i));
                                if (Decompress_mw_s1.isPreferenceOn(cell.getText(), i)) {
                                    prefMenuItem.setSelected(true);
                                }

                                prefMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> obs1, Boolean old_val, Boolean new_val) {
                                        final String new_ppr;
                                        final Service service = (Service) cell.getTableRow().getItem();

                                        if (new_val == true) {
                                            new_ppr = Decompress_mw_s1.add_ppr(cell.getText(), line);
                                        }
                                        else {
                                            new_ppr = Decompress_mw_s1.remove_ppr(cell.getText(), line);
                                        }
                                        
                                        try {
                                            service.setS_ppr(new_ppr);
                                            service.setS_flag(true);
                                            bdd.update_bdd(service);
                                        }
                                        catch (HibernateException e) {
                                            error_msg.Error_diag("Error update BDD\n"+e.getMessage());
                                        }
                                    }
                                });

                                cellMenu.getItems().add(prefMenuItem);
                                cell.setContextMenu(cellMenu);
                            }
                        } else {
                            cell.setContextMenu(null);
                        }
                    }
                });
                return cell;
            }
        });
        
        // Editable service name
        s_nameColumn.setCellFactory(new Callback<TableColumn<Service, String>, TableCell<Service, String>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new EditingCell();
            }
        });
        s_nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Service, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Service, String> t) {
                final Service service = ((Service) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                
                try {
                    service.setS_name(t.getNewValue());
                    service.setS_flag(true);
                    bdd.update_bdd(service);
                }
                catch (HibernateException e) {
                    error_msg.Error_diag("Error update BDD\n"+e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleImportAction(ActionEvent event) throws Exception {

        Decompress_mw_s1 decompress = new Decompress_mw_s1();
        String chemin = "D:\\Info\\Misc\\Vigica\\dvb_s_mw_s1";

        stage = (Stage) serviceTable.getScene().getWindow();
        fileChooser.setTitle("Open Services File");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            
            chemin = file.getAbsolutePath();

            pi.visibleProperty().bind(decompress.decompressTask.runningProperty());
            pi.progressProperty().bind(decompress.decompressTask.progressProperty());
            decompress.decompressTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    // print services into tableview
                    serviceData.setAll(decompress.decompressTask.getValue());
                    serviceTable.setItems(serviceData);
                    title.setText(file.getName());
                }
            });
            decompress.decompressTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    error_msg.Error_diag("Error save BDD\n" + decompress.decompressTask.getException().getMessage());
                }
            });
            

            decompress.decompressTask.setChemin(chemin);
            new Thread(decompress.decompressTask).start();
        }
        else
            error_msg.Error_diag("No file choosed\n");
    }

    @FXML
    private void handleCompareAction(ActionEvent event) throws Exception {

        final Compare_mw_s1 compare = new Compare_mw_s1();
        String chemin = "D:\\Info\\Misc\\Vigica\\dvb_s_mw_s1_old";

        stage = (Stage) serviceTable.getScene().getWindow();
        if (serviceData.size() == 0) {
            error_msg.Error_diag("No service file loaded\n");
            return;
        }
        fileChooser.setTitle("Open Old Services");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            chemin = file.getAbsolutePath();
            
            pi.visibleProperty().bind(compare.compareTask.runningProperty());
            pi.progressProperty().bind(compare.compareTask.progressProperty());
            compare.compareTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    // print services into tableview
                    serviceData.setAll(compare.compareTask.getValue());
                }
            });
            compare.compareTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    error_msg.Error_diag("Error compare services\n" + compare.compareTask.getException().getMessage());
                }
            });
            

            compare.compareTask.setChemin(chemin);
            new Thread(compare.compareTask).start();
        }
        else
            error_msg.Error_diag("No file choosed\n");
    }
    
    @FXML
    private void handleExportAction(ActionEvent event) {
        final Generate_mw_s1 generate = new Generate_mw_s1();
        String chemin = "D:\\Info\\Misc\\Vigica\\dvb_s_mw_s1";

        stage = (Stage) serviceTable.getScene().getWindow();
        if (serviceData.size() == 0) {
            error_msg.Error_diag("No service file loaded\n");
            return;
        }
        fileChooser.setTitle("Export Services");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            chemin = file.getAbsolutePath();
            
            pi.visibleProperty().bind(generate.generateTask.runningProperty());
            pi.progressProperty().bind(generate.generateTask.progressProperty());
            generate.generateTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    error_msg.Info_diag("Services exported");
                }
            });
            generate.generateTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent t) {
                    error_msg.Error_diag("Error compare services\n" + generate.generateTask.getException().getMessage());
                }
            });
            

            generate.generateTask.setChemin(chemin);
            new Thread(generate.generateTask).start();
        }
        else
            error_msg.Error_diag("No file choosed\n");
    }
    
    @FXML
    private void handleFilterAction(ActionEvent event) {
        
        ArrayList <Service> services = new ArrayList();;
        
        try{
            String sql = "FROM Service WHERE 1=1 ";
            if (s_idx.getText().length() > 0)
                sql += " AND IDX = " + s_idx.getText();
            if (s_type.getText().length() > 0)
                sql += " AND TYPE like '" + s_type.getText() + "'";
            if (s_name.getText().length() > 0)
                sql += " AND UPPER(NAME) like UPPER('" + s_name.getText() + "')";
            sql += " ORDER BY IDX";
            
            services = bdd.read_bdd(sql);
            serviceData.setAll(services);
            
        }catch (HibernateException e) {
            error_msg.Error_diag("Error read BDD\n"+e.getMessage());
        }
    }
    
    @FXML
    private void handleDuplicateAction(ActionEvent event) {

        Decompress_mw_s1 decompress = new Decompress_mw_s1();
        
        pi.visibleProperty().bind(decompress.duplicateTask.runningProperty());
        pi.progressProperty().bind(decompress.duplicateTask.progressProperty());
        decompress.duplicateTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                // print services into tableview
                serviceData.setAll(decompress.duplicateTask.getValue());
            }
        });
        decompress.duplicateTask.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                error_msg.Error_diag("Error remove duplicate services\n" + decompress.duplicateTask.getException().getMessage());
            }
        });


        decompress.duplicateTask.setServices(serviceData);
        new Thread(decompress.duplicateTask).start();
    }
    
    class EditingCell extends TableCell<Service, String> {

        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }

            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
    
}
