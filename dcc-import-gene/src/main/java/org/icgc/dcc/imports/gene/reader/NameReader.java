/*
 * Copyright (c) 2016 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.imports.gene.reader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import lombok.SneakyThrows;
import lombok.val;

/**
 * Reads display names for genes and constructs map of symbol to gene name
 */
public final class NameReader {

  /**
   * Constants
   */
  private static final String XREF_URI =
      "ftp://ftp.ensembl.org/pub/grch37/release-82/mysql/homo_sapiens_core_82_37/xref.txt.gz";
  private static final Pattern TSV = Pattern.compile("\t");

  /**
   * Get the map of xref display id -> gene name
   */
  @SneakyThrows
  public static Map<String, String> readXrefDisplay() {
    val gzip = new GZIPInputStream(new URL(XREF_URI).openStream());
    val inputStreamReader = new InputStreamReader(gzip);
    val bufferedReader = new BufferedReader(inputStreamReader);

    val retMap = new HashMap<String, String>();
    for (String s = bufferedReader.readLine(); null != s; s = bufferedReader.readLine()) {
      s = s.trim();
      if (s.length() > 0) {
        String[] line = TSV.split(s);

        // Only use the gene wiki rows.
        if ("12600".equals(line[1])) {
          val symbol = line[3];
          val name = line[5];
          retMap.put(symbol, name);
        }

      }
    }

    return retMap;
  }

}
