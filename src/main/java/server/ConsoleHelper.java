package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
     public static void writeMessage(String message) {
         System.out.println(message);
     }

     public static String readString() { // Ввод строк из консоли т.е читаем из консоли строки.
         while(true) {
             try {
                 String buffer = bufferedReader.readLine();
                 if(buffer != null)
                     return buffer;
             } catch (IOException e) {
                 writeMessage("Произошла ошибка при вводе текста. Попробуйте ещё раз");
             }
         }
     }

     public static int readInt() { // Ввод чисел из консоли т.е читаем из консли числа.
         while (true) {
             try {
                 return Integer.parseInt(readString().trim());
             } catch (NumberFormatException e) {
                 writeMessage("Произошла ошибка при вводе чисел. Попробуйте ещё раз");
             }
         }
     }
}


/**
 *
 *  Данный класс server.ConsoleHelper представляет собой утилиту для чтения введенных пользователем
 *  строк и чисел с консоли.
 *
 *  Он содержит следующие методы:
 *
 *  writeMessage(String message): выводит в консоль переданную в качестве аргумента строку сообщения.
 *
 *  readString(): считывает строку, введенную пользователем в консоли, и возвращает ее в качестве результата.
 *  Если при чтении строки возникает ошибка, метод выдает сообщение об ошибке и продолжает ожидать ввод.
 *
 *  readInt(): считывает целое число, введенное пользователем в консоли, и возвращает его в качестве результата.
 *  Если при чтении числа возникает ошибка, метод выдает сообщение об ошибке и продолжает ожидать ввода.
 *
 *  Оба метода readString() и readInt() работают в бесконечном цикле, который продолжает запрашивать ввод,
 *  пока не будет получен корректный ввод.
 *  При получении ввода метод readString() удаляет начальные и конечные пробелы из введенной строки
 *  с помощью метода trim(), а затем возвращает ее как результат.
 *  Метод readInt() использует метод readString() для чтения введенной пользователем строки,
 *  затем преобразует ее в целое число с помощью метода Integer.parseInt() и возвращает его в качестве результата.
 *
 */