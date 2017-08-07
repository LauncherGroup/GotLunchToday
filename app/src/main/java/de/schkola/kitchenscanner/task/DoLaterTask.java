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
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.schkola.kitchenscanner.task;

import android.os.AsyncTask;

/**
 * Dieser Task wird ausgeführt, wenn das Blitzlicht erscheint
 */
public class DoLaterTask extends AsyncTask<Void, Void, Void> {

    private long millis;
    private Runnable before = null;
    private Runnable after;

    public DoLaterTask(long millis, Runnable after) {
        this.millis = millis;
        this.after = after;
    }

    public DoLaterTask(long millis, Runnable before, Runnable after) {
        this.millis = millis;
        this.before = before;
        this.after = after;
    }

    @Override
    protected void onPreExecute() {
        if (before != null) {
            before.run();
        }
    }

    @Override
    protected Void doInBackground(Void... Void) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        after.run();
    }
}