/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * SyncPanel.java
 *
 * Created on 19-dic-2011, 8:18:49
 */
package com.openbravo.pos.smj;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.LocalRes;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.util.AltEncrypter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.swing.JComponent;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import org.apache.activemq.ActiveMQConnectionFactory;
/**
 * clase para lanzamiento de sicronizacion
 * class to launch synchronization
 * @author Carlos Prieto - SmartJSP S.A.S.
 */
public class SyncPanel extends javax.swing.JPanel implements BeanFactoryApp, JPanelView,  ActionListener  {
    private DataLogicSystem dlsystem;
    private AppView app;
    private Timer timer;

    /** Creates new form SyncPanel */
    public SyncPanel() {
        initComponents();
        jButton1.setText(AppLocal.getIntString("Menu.Sync"));
    }
    
    public void init(AppView app) throws BeanFactoryException {
        this.app = app;
        dlsystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        timer = new Timer(15000, this);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));
        setMinimumSize(new java.awt.Dimension(485, 413));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jButton1.setText(bundle.getString("label.synchronize")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(174, 174, 174)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 728, Short.MAX_VALUE))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 193, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    /**
     * sent synchronization message  to the ERP
     * @param evt 
     */
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try {
            dlsystem.setResource("jms.message",0,"".getBytes());
            dlsystem.setResource("jms.error",0,"".getBytes());
            int action  = JOptionPane.showConfirmDialog(null, LocalRes.getIntString("message.sync"),"", JOptionPane.YES_NO_OPTION);
            if(action == JOptionPane.YES_OPTION){
                String subject = dlsystem.getResourceAsText("jms.outqueue");
                String org = dlsystem.getResourceAsText("jms.inqueue");
                String url = dlsystem.getResourceAsText("jms.url.out");
                String userLogin = dlsystem.getResourceAsText("jms.userLogin");
                String password = dlsystem.getResourceAsText("jms.password");

                if(password.indexOf("crypt") == 0){
                    AltEncrypter cypher = new AltEncrypter("cypherkey" + userLogin);
                    password = cypher.decrypt(password.substring(6));
                }

                ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(userLogin, password, url);
                Connection connection = connectionFactory.createConnection();
                connection.start();

                Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);

                Destination destination = session.createQueue(subject);
                MessageProducer producer = session.createProducer(destination);

                String xml ="";
                xml += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
                xml += "<entityDetail>";
                xml += "	<type>synchronization-request</type>";
                xml += "	<detail>";
                xml += "	<org>"+org+"</org>";
                xml += "    <client>"+subject.substring(3) +"</client>";
                xml += "    <pcTerminal>"+app.getProperties().getProperty("machine.hostname") +"</pcTerminal>";
                xml += "	</detail>";
                xml += "</entityDetail>";
                Logger.getLogger(SyncPanel.class.getName()).log(Level.INFO, "++++++++++++++++++++\n");
      //          Logger.getLogger(SyncPanel.class.getName()).log(Level.INFO, "Synchronizartin starts");  
                Logger.getLogger(SyncPanel.class.getName()).log(Level.INFO,
                        AppLocal.getIntString("label.synchronizeStart"));   // smj
                
                Logger.getLogger(SyncPanel.class.getName()).log(Level.INFO, xml);
                Logger.getLogger(SyncPanel.class.getName()).log(Level.INFO, "++++++++++++++++++++\n");
                TextMessage message = session.createTextMessage(xml);
                producer.send(message);
                connection.close();
                jLabel1.setText(AppLocal.getIntString("label.synchronizeStart2"));  // smj
                timer.start();
             
                jButton1.setEnabled(false);
            }
        } catch (JMSException ex) {
            Logger.getLogger(SyncPanel.class.getName()).log(Level.FINE, null, ex);
            jLabel1.setText(AppLocal.getIntString("label.synchronizeConnect"));  // smj
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables

    @Override
    public Object getBean() {
        return this;
    }   

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.Sync");
    }

    @Override
    public void activate() throws BasicException {
        
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     * displays error messages and completion
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String jmsMessage = dlsystem.getResourceAsText("jms.message").trim();
        String message;
        if(jmsMessage.equalsIgnoreCase("SYNC-END")){
            message = jTextArea1.getText();
            message +="\n\n ++++++++++++++++\n";
            message += AppLocal.getIntString("label.synchronizeFinish");  // smj
            message +="\n ++++++++++++++++\n";
                    
            jTextArea1.setText(message);
            timer.stop();
            jButton1.setEnabled(true);
            jLabel1.setText(AppLocal.getIntString("label.synchronizeFinish"));  // smj
        }else if(jmsMessage.equalsIgnoreCase("SYNC-END-WITH-ERRORS")){
            message = jTextArea1.getText();
            message +="\n\n ++++++++++++++++\n";
            message += AppLocal.getIntString("label.synchronizeERPErrors");  // smj
            message +="\n ++++++++++++++++\n";
                    
            jTextArea1.setText(message);
            timer.stop();
            jButton1.setEnabled(true);
        }else if(jmsMessage.equalsIgnoreCase("SYNC-ERROR")){
            //message = jTextArea1.getText();
            message = AppLocal.getIntString("label.synchronizePOSErrors");    // smj
            String error = dlsystem.getResourceAsText("jms.error");
            message += "\n"+error; 
            jTextArea1.setText(message);
            dlsystem.setResource("jms.message",0,"".getBytes());        
        }
    }
}
