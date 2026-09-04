package edu.umg.programacion2.clase07.biblioteca.dao;

import edu.umg.programacion2.clase07.biblioteca.modelo.Prestamo;
import edu.umg.programacion2.clase07.biblioteca.modelo.PrestamoDetalle;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de prestamos. Los primeros dos metodos (registrarPrestamo,
 * marcarDevuelto) son puro repaso de la Clase 5: un INSERT y un UPDATE con
 * PreparedStatement, exactamente como en EstudianteDAO.
 *
 * El tercer metodo, listarPrestamosActivosConLibro(), es el ejercicio nuevo
 * de esta clase: una consulta que combina DOS tablas con JOIN.
 */
public class PrestamoDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/prog2_db?useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "tu_password_aqui";

    // Repaso: INSERT con generated keys, igual que EstudianteDAO.crear().
    public int registrarPrestamo(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos (libro_id, nombre_estudiante, fecha_prestamo, fecha_devolucion) "
                + "VALUES (?, ?, ?, ?)";

        try (Connection conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             PreparedStatement statement = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, prestamo.getLibroId());
            statement.setString(2, prestamo.getNombreEstudiante());
            statement.setDate(3, Date.valueOf(prestamo.getFechaPrestamo()));
            statement.setNull(4, java.sql.Types.DATE);
            statement.executeUpdate();

            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
                return -1;
            }
        }
    }

    // Repaso: UPDATE simple, igual que EstudianteDAO.actualizarNombre().
    public boolean marcarDevuelto(int prestamoId, LocalDate fechaDevolucion) throws SQLException {
        String sql = "UPDATE prestamos SET fecha_devolucion = ? WHERE id = ? AND fecha_devolucion IS NULL";

        try (Connection conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             PreparedStatement statement = conexion.prepareStatement(sql)) {

            statement.setDate(1, Date.valueOf(fechaDevolucion));
            statement.setInt(2, prestamoId);

            int filasAfectadas = statement.executeUpdate();
            return filasAfectadas > 0;
        }
    }
    public List<PrestamoDetalle> listarPrestamosActivosConLibro() throws SQLException {
        List<PrestamoDetalle> resultado = new ArrayList<>();

        String sql = "SELECT p.nombre_estudiante, p.fecha_prestamo, l.titulo "
                + "FROM prestamos p "
                + "JOIN libros l ON p.libro_id = l.id "
                + "WHERE p.fecha_devolucion IS NULL "
                + "ORDER BY p.fecha_prestamo";

        try (Connection conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             PreparedStatement statement = conexion.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                String titulo = rs.getString("titulo");
                String nombreEstudiante = rs.getString("nombre_estudiante");
                LocalDate fechaPrestamo = rs.getDate("fecha_prestamo").toLocalDate();

                PrestamoDetalle detalle = new PrestamoDetalle(
                        titulo,
                        nombreEstudiante,
                        fechaPrestamo
                );

                resultado.add(detalle);
            }
        }

        return resultado;
    }}
