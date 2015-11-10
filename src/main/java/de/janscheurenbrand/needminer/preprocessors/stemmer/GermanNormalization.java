package de.janscheurenbrand.needminer.preprocessors.stemmer;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Normalizes German characters according to the heuristics
 * of the <a href="http://snowball.tartarus.org/algorithms/german2/stemmer.html">
 * German2 snowball algorithm</a>.
 * It allows for the fact that ä, ö and ü are sometimes written as ae, oe and ue.
 * <ul>
 *   <li> 'ß' is replaced by 'ss'
 *   <li> 'ä', 'ö', 'ü' are replaced by 'a', 'o', 'u', respectively.
 *   <li> 'ae' and 'oe' are replaced by 'a', and 'o', respectively.
 *   <li> 'ue' is replaced by 'u', when not following a vowel or q.
 * </ul>
 * This is useful if you want this normalization without using
 * the German2 stemmer, or perhaps no stemming at all.
 */
public class GermanNormalization {
    // FSM with 3 states:
    private static final int N = 0; /* ordinary state */
    private static final int V = 1; /* stops 'u' from entering umlaut state */
    private static final int U = 2; /* umlaut state, allows e-deletion */

    public String normalize(String input) {
        int state = N;
        char buffer[] = input.toCharArray();
        int length = input.length();
        for (int i = 0; i < length; i++) {
            final char c = buffer[i];
            switch (c) {
                case 'a':
                case 'o':
                    state = U;
                    break;
                case 'u':
                    state = (state == N) ? U : V;
                    break;
                case 'e':
                    if (state == U)
                        length = delete(buffer, i--, length);
                    state = V;
                    break;
                case 'i':
                case 'q':
                case 'y':
                    state = V;
                    break;
                case 'ä':
                    buffer[i] = 'a';
                    state = V;
                    break;
                case 'ö':
                    buffer[i] = 'o';
                    state = V;
                    break;
                case 'ü':
                    buffer[i] = 'u';
                    state = V;
                    break;
                case 'ß':
                    buffer[i++] = 's';
                    buffer = resizeBuffer(buffer, 1 + length);
                    if (i < length)
                        System.arraycopy(buffer, i, buffer, i + 1, (length - i));
                    buffer[i] = 's';
                    length++;
                    state = N;
                    break;
                default:
                    state = N;
            }
        }
        return new String(buffer);
    }

    public static int delete(char s[], int pos, int len) {
        assert pos < len;
        if (pos < len - 1) { // don't arraycopy if asked to delete last character
            System.arraycopy(s, pos + 1, s, pos, len - pos - 1);
        }
         return len - 1;
    }

    public final char[] resizeBuffer(char[] termBuffer, int newSize) {
        if(termBuffer.length < newSize){
            final char[] newCharBuffer = new char[newSize];
            System.arraycopy(termBuffer, 0, newCharBuffer, 0, termBuffer.length);
            termBuffer = newCharBuffer;
        }
        return termBuffer;
    }
}