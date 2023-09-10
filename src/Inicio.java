import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Inicio {

    int numero = 0;
    public Inicio(){
        JFrame marco = new JFrame();
        JLabel rotnombre = new JLabel("Nombre: ", SwingConstants.CENTER);
        String nombre = new String();
        JButton botjugar = new JButton("Jugar!");
        JTextField camponombre = new JTextField();
        JPanel titulo = new JPanel();
        JLabel rottitulo = new JLabel("Dot.Game", SwingConstants.CENTER);
        JPanel opciones = new JPanel();


        titulo.setBorder(BorderFactory.createEmptyBorder(100, 100, 100, 100));
        titulo.setLayout(new GridLayout(0, 1));
        rottitulo.setFont(rottitulo.getFont().deriveFont(64.0f));
        titulo.add(rottitulo);

        opciones.setBorder(BorderFactory.createEmptyBorder(100, 100, 100, 100));
        opciones.setLayout(new GridLayout(0, 1));
        opciones.add(rotnombre);
        opciones.add(camponombre);
        opciones.add(botjugar);





        botjugar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent evt) {

                numero = 1;
                marco.dispose();






            }
        });




        marco.add(titulo, BorderLayout.NORTH);
        marco.add(opciones, BorderLayout.CENTER);
        marco.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        marco.setTitle("Our GUI");
        marco.pack();
        marco.setVisible(true);
    }
    public static void main(String[] args){
        new Inicio();
    }





}