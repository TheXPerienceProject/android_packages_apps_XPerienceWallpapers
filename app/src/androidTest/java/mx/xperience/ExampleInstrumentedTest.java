/*
 * Copyright (c) 2021. The XPerience Project 
 *  
 *  Licensed under the GNU GPLv2 license
 *  
 *   The text of the license can be found in the LICENSE file
 *   or at https://www.gnu.org/licenses/gpl-2.0.txt
 */

package mx.xperience;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("mx.xperience", appContext.getPackageName());
    }
}