/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jxboxdatasave;

/**
 *
 * @author Slam
 */
class STFSFileEntry {

    String filename;
    int size;
    int startingBlock;
    byte[] content;
}

// A partir de cierto offset, empieza la tabla de archivos y sus bloques de datos.
// Implementar requiere parseo de hash tables y cadena de bloques.

