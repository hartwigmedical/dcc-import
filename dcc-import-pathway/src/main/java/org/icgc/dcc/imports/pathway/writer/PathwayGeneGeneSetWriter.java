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
package org.icgc.dcc.imports.pathway.writer;

import static org.icgc.dcc.common.core.util.Formats.formatCount;
import static org.icgc.dcc.imports.geneset.model.GeneSetType.PATHWAY;

import org.icgc.dcc.imports.geneset.writer.AbstractGeneGeneSetWriter;
import org.icgc.dcc.imports.pathway.core.PathwayModel;
import org.icgc.dcc.imports.pathway.util.PathwayGeneGeneSetsBuilder;
import org.jongo.MongoCollection;

import lombok.NonNull;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PathwayGeneGeneSetWriter extends AbstractGeneGeneSetWriter {

  public PathwayGeneGeneSetWriter(@NonNull MongoCollection geneCollection) {
    super(geneCollection, PATHWAY);
  }

  public void write(@NonNull PathwayModel model) {
    log.info("Clearing pathways from gene document...");
    clearGeneGeneSets();

    val geneGeneSetsBuilder = new PathwayGeneGeneSetsBuilder(model);

    log.info("Adding pathways to gene documents...");

    int updateGeneCount = 0;
    for (val gene : getGenes()) {
      val geneSets = geneGeneSetsBuilder.build(gene);

      if (!geneSets.isEmpty()) {
        addGeneSets(gene, geneSets);
        saveGene(gene);

        updateGeneCount++;
        val status = updateGeneCount % 1000 == 0;
        if (status) {
          log.info("Updated pathways for {} genes", formatCount(updateGeneCount));
        }
      }
    }

    log.info("Finished writing pathways for {} genes total", formatCount(updateGeneCount));
  }

}
