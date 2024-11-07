package pruebaUDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class servidor {
    public static void main(String[] args) {
        int puerto = 3000;
        DatagramSocket socket = null;
        
        try {
            socket = new DatagramSocket(puerto);
            System.out.println("Servidor encendido en puerto " + puerto);

            while (true) {
                // Buffer para recibir los datos del cliente
                byte[] bufferEntrada = new byte[1024];
                DatagramPacket paqueteEntrada = new DatagramPacket(bufferEntrada, bufferEntrada.length);

                // Recibir mensaje de saludo del cliente
                socket.receive(paqueteEntrada);
                System.out.println("Cliente conectado desde: " + paqueteEntrada.getAddress() + ":" + paqueteEntrada.getPort());

                // Crear un nuevo hilo con su propio socket
                DatagramSocket socketHilo = new DatagramSocket();
                HiloCliente hilo = new HiloCliente(socketHilo, paqueteEntrada);
                hilo.start();

                // Enviar el puerto del nuevo socket al cliente
                String mensajePuerto = String.valueOf(socketHilo.getLocalPort());
                byte[] bufferPuerto = mensajePuerto.getBytes();
                DatagramPacket paquetePuerto = new DatagramPacket(
                    bufferPuerto,
                    bufferPuerto.length,
                    paqueteEntrada.getAddress(),
                    paqueteEntrada.getPort()
                );
                socket.send(paquetePuerto);
            }

        } catch (SocketException e) {
            System.err.println("Error: No se puede iniciar el servidor en el puerto " + puerto);
            System.err.println("Causa: " + e.getMessage());
            System.err.println("Asegúrese de que no hay otra instancia del servidor ejecutándose");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
