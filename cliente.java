package pruebaUDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class cliente {
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress IP_servidor = InetAddress.getByName("localhost");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                // Enviar mensaje inicial al servidor
                String mensajeInicial = "Conectar";
                byte[] bufferInicial = mensajeInicial.getBytes();
                DatagramPacket paqueteInicial = new DatagramPacket(bufferInicial, bufferInicial.length, IP_servidor, 3000);
                socket.send(paqueteInicial);

                // Recibir el puerto asignado
                byte[] bufferPuerto = new byte[1024];
                DatagramPacket paquetePuerto = new DatagramPacket(bufferPuerto, bufferPuerto.length);
                socket.receive(paquetePuerto);
                String mensajePuerto = new String(paquetePuerto.getData(), 0, paquetePuerto.getLength());
                int puertoAsignado = Integer.parseInt(mensajePuerto.trim());

                // Jugar las 5 preguntas
                for (int i = 0; i < 5; i++) {
                    // Recibir pregunta
                    byte[] bufferEntrada = new byte[1024];
                    DatagramPacket paqueteEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);
                    socket.receive(paqueteEntrada);
                    String pregunta = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength());

                    // Mostrar pregunta y obtener respuesta
                    System.out.println("\nPregunta " + (i+1) + ": " + pregunta);
                    System.out.print("Tu respuesta: ");
                    String respuesta = scanner.nextLine();

                    // Enviar respuesta
                    byte[] bufferSalida = respuesta.getBytes();
                    DatagramPacket paqueteSalida = new DatagramPacket(
                        bufferSalida,
                        bufferSalida.length,
                        IP_servidor,
                        puertoAsignado
                    );
                    socket.send(paqueteSalida);

                    // Recibir resultado
                    socket.receive(paqueteEntrada);
                    String resultado = new String(paqueteEntrada.getData(), 0, paqueteEntrada.getLength());
                    System.out.println(resultado);
                }

                // Recibir puntuación final
                byte[] bufferFinal = new byte[1024];
                DatagramPacket paqueteFinal = new DatagramPacket(bufferFinal, bufferFinal.length);
                socket.receive(paqueteFinal);
                String mensajeFinal = new String(paqueteFinal.getData(), 0, paqueteFinal.getLength());
                System.out.println("\n" + mensajeFinal);
                break;
            }

            System.out.println("\n¡Gracias por jugar!");
            scanner.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
