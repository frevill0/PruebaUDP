package pruebaUDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HiloCliente extends Thread {
    private DatagramSocket socket;
    private DatagramPacket paqueteEntrada;
    private String[][] preguntasRespuestas = {
        {"Cual es el nombre de la hermana de Jinx?", "Vi"},
        {"En que ciudad esta ubicada Zaun?", "Piltover"},
        {"De que color es el pelo de Lux?", "rubio"},
        {"Como se llama el arma de Jinx?", "Pium Pium"},
        {"Cual es la faccion opuesta a Noxus?", "Demacia"}
    };
    private int puntuacion = 0;
    private static final int PUNTOS_POR_PREGUNTA = 4;
    private static final int TOTAL_PREGUNTAS = 5;
    private StringBuilder resultadosCuestionario = new StringBuilder();
    private static final String NOMBRE_ARCHIVO = "resultados_cuestionarios.txt";

    public HiloCliente(DatagramSocket socket, DatagramPacket paqueteEntrada) {
        this.socket = socket;
        this.paqueteEntrada = paqueteEntrada;
    }

    @Override
    public void run() {
        try {
            // Iniciar el archivo de resultados
            LocalDateTime fecha = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            resultadosCuestionario.append("\n=== resultado del cuestionario ===\n");
            resultadosCuestionario.append("fecha: ").append(fecha.format(formatter)).append("\n");
            resultadosCuestionario.append("ip cliente: ").append(paqueteEntrada.getAddress().getHostAddress()).append("\n\n");

            // Iterar sobre las preguntas en orden
            for (int i = 0; i < TOTAL_PREGUNTAS; i++) {
                String pregunta = preguntasRespuestas[i][0];
                String respuestaCorrecta = preguntasRespuestas[i][1];

                // Enviar pregunta al cliente
                byte[] bufferPregunta = pregunta.getBytes();
                DatagramPacket paquetePregunta = new DatagramPacket(
                    bufferPregunta,
                    bufferPregunta.length,
                    paqueteEntrada.getAddress(),
                    paqueteEntrada.getPort()
                );
                socket.send(paquetePregunta);
                System.out.println("Pregunta " + (i+1) + " enviada al cliente: " + pregunta);

                // Recibir respuesta del cliente
                byte[] bufferRespuesta = new byte[1024];
                DatagramPacket paqueteRespuesta = new DatagramPacket(bufferRespuesta, bufferRespuesta.length);
                socket.receive(paqueteRespuesta);
                
                String respuestaRecibida = new String(paqueteRespuesta.getData(), 0, paqueteRespuesta.getLength()).trim().toLowerCase();
                
                // Verificar respuesta y enviar resultado
                boolean esCorrecta = respuestaRecibida.equalsIgnoreCase(respuestaCorrecta);
                if (esCorrecta) {
                    puntuacion += PUNTOS_POR_PREGUNTA;
                }
                
                String resultado = esCorrecta ? 
                    "¡Correcto!" : 
                    "Incorrecto. La respuesta correcta era: " + respuestaCorrecta;

                // Guardar resultado en el StringBuilder
                resultadosCuestionario.append("pregunta ").append(i + 1).append(": ").append(pregunta).append("\n");
                resultadosCuestionario.append("respuesta del cliente: ").append(respuestaRecibida).append("\n");
                resultadosCuestionario.append("respuesta correcta: ").append(respuestaCorrecta).append("\n");
                resultadosCuestionario.append("estado: ").append(esCorrecta ? "correcta" : "incorrecta").append("\n\n");
                
                byte[] bufferResultado = resultado.getBytes();
                DatagramPacket paqueteResultado = new DatagramPacket(
                    bufferResultado,
                    bufferResultado.length,
                    paqueteEntrada.getAddress(),
                    paqueteEntrada.getPort()
                );
                socket.send(paqueteResultado);
            }

            // Añadir puntuación final al archivo
            resultadosCuestionario.append("puntaje final: ").append(puntuacion).append(" de 20\n");
            resultadosCuestionario.append("================================\n");

            // Guardar en archivo (agregando al final)
            try (PrintWriter writer = new PrintWriter(new FileWriter(NOMBRE_ARCHIVO, true))) {
                writer.print(resultadosCuestionario.toString());
                writer.flush();
            }

            // Enviar puntuación final al cliente
            String mensajeFinal = String.format("Juego terminado. Tu puntuación final es: %d/20", puntuacion);
            byte[] bufferFinal = mensajeFinal.getBytes();
            DatagramPacket paqueteFinal = new DatagramPacket(
                bufferFinal,
                bufferFinal.length,
                paqueteEntrada.getAddress(),
                paqueteEntrada.getPort()
            );
            socket.send(paqueteFinal);
            
            // Cerrar el socket
            socket.close();
            System.out.println("Cliente desconectado: " + paqueteEntrada.getAddress() + ":" + paqueteEntrada.getPort());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
