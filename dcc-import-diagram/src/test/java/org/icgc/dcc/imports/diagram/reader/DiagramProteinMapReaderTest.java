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
package org.icgc.dcc.imports.diagram.reader;

import static org.assertj.core.api.Assertions.assertThat;

import org.icgc.dcc.imports.diagram.reader.DiagramProteinMapReader;

import lombok.val;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class DiagramProteinMapReaderTest {

  @Test
  public void testProteinMapReader() throws Exception {
    val reader = new DiagramProteinMapReader();
    val result = reader.readProteinMap("4839726");

    assertThat(result.get("5218942")).isIn(
        ImmutableList.of("UniProt:Q71DI3", "UniProt:P68431"),
        ImmutableList.of("UniProt:P68431", "UniProt:Q71DI3"));
    assertThat(result.get("181902")).isEqualTo(ImmutableList.of("UniProt:P62805"));
    assertThat(result.get("77087")).isNull();
  }

}
