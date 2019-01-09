/*
 * MIT License
 *
 * Copyright 2016 Niklas Merkelt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.schkola.kitchenscanner.util;

import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contains the data of a Person
 */
public class Person {

    private static final SparseArray<Person> all = new SparseArray<>();
    private final String person_name;
    private final String clazz;
    private final byte lunch;
    private final File f;
    private ArrayList<String> allergies;
    private byte gotLunch = -1;

    public Person(int xba, String clazz, String name, byte lunch, File path) {
        this.clazz = clazz;
        this.person_name = name;
        this.lunch = lunch;
        all.put(xba, this);
        f = new File(path, xba + ".txt");
    }

    public static Person getByXBA(int xba) {
        return all.get(xba);
    }

    public static SparseArray<Person> getPersons() {
        return all;
    }

    private File getLunchFile() {
        return f;
    }

    public String getClazz() {
        return clazz.equals("Mitarbeiter") ? clazz : "Klasse " + clazz;
    }

    public String getLunch() {
        switch (lunch) {
            case 1:
                return "A";
            case 2:
                return "B";
            case 3:
                return "S";
            default:
                return "X";
        }
    }

    public byte getRawLunch() {
        return lunch;
    }

    public String getPersonName() {
        return person_name;
    }

    public byte getGotLunch() {
        if (gotLunch == -1) {
            try {
                BufferedReader buffer = new BufferedReader(new FileReader(getLunchFile()));
                String lunch = buffer.readLine();
                buffer.close();
                gotLunch = Byte.parseByte(lunch);
            } catch (Exception e) {
                gotLunch = 0;
            }
        }
        return gotLunch;
    }

    public String getAllergies() {
        if (allergies == null) {
            return "";
        }
        return Arrays.toString(allergies.toArray()).replaceAll("([\\[\\]])", "");
    }

    public void addAllergy(String s) {
        if (allergies == null) {
            allergies = new ArrayList<>();
        }
        allergies.add(s);
    }

    public void gotLunch() {
        Log.d("Person", "gotLunch called!");
        gotLunch = (byte) (getGotLunch() + 1);
        getLunchFile().delete();
        try {
            FileOutputStream fos = new FileOutputStream(getLunchFile());
            PrintWriter pw = new PrintWriter(fos);
            pw.println(gotLunch);
            pw.flush();
            pw.close();
            fos.close();
        } catch (IOException ex) {
            Log.e("Person", "Exception gotLunch", ex);
        }
    }

    public void resetGotLunch() {
        gotLunch = -1;
    }
}
