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

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import org.hibernate.Session;
import org.hibernate.Transaction;
import vigica_edit.Decompress_mw_s1;
import vigica_edit.HibernateUtil;
import vigica_edit.model.Service;

/**
 *
 * @author bnabi
 */
public class FXMLMainController implements Initializable {
    
    private Decompress_mw_s1 decompress = new Decompress_mw_s1();
    static private Error_Msg error_msg = new Error_Msg();
    /**
    * The data as an observable list of Service.
    */
    private ObservableList<Service> serviceData = FXCollections.observableArrayList();
    
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


    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public FXMLMainController() {
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        s_idxColumn.setCellValueFactory(cellData -> cellData.getValue().s_idxProperty().asObject());
        s_nameColumn.setCellValueFactory(cellData -> cellData.getValue().s_nameProperty());
        s_typeColumn.setCellValueFactory(cellData -> cellData.getValue().s_typeProperty());
        s_nidColumn.setCellValueFactory(cellData -> cellData.getValue().s_nidProperty().asObject());
        s_pprColumn.setCellValueFactory(cellData -> cellData.getValue().s_pprProperty());
        
        // Context menu
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
                                final CheckMenuItem prefMenuItem = new CheckMenuItem("pref"+i);
                                final int line = i;

                                prefMenuItem.setId(String.valueOf(i));
                                if (decompress.isPreferenceOn(cell.getText(), i)) {
                                    prefMenuItem.setSelected(true);
                                }

                                prefMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> obs1, Boolean old_val, Boolean new_val) {
                                        final String new_ppr;
                                        final Service service = (Service) cell.getTableRow().getItem();
                                        String hhh=prefMenuItem.getId();
                                        if (new_val == true) {
                                            new_ppr = decompress.add_ppr(cell.getText(), line);
                                        }
                                        else {
                                            new_ppr = decompress.remove_ppr(cell.getText(), line);
                                        }
                                        
                                        try {
                                            service.setS_ppr(new_ppr);
                                            decompress.update_bdd(service);
                                        }
                                        catch (Exception e) {
                                            error_msg.Error_diag("Error update BDD\n"+e.getCause().getMessage());
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
    }
    
    @FXML
    private void handleImportAction(ActionEvent event) throws Exception {

        ArrayList <Service> services;
        final String chemin = "D:\\Info\\Misc\\Vigica\\dvb_s_mw_s1";

        decompress.decompress(chemin);
        services = decompress.getServices();

        // Add to database
        //Get the session from the session factory.
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try{
            // begin the transaction from the sessiom
            tx = session.beginTransaction();
            
            for(Service service : services){
                session.save(service);
            }

            //The changes to persistent object will be written to database.
            tx.commit();

            // print services into tableview
            serviceData.setAll(services);
            serviceTable.setItems(serviceData);
        }catch (Exception e) {
            error_msg.Error_diag(e.getCause().getMessage());
            if (tx!=null) tx.rollback();
        }finally {
            // close the session
            session.close();
        }
    }

    @FXML
    private void handleCompareAction(ActionEvent event) {
        
    }
    
    @FXML
    private void handleExportAction(ActionEvent event) {

    }
    

    
}
