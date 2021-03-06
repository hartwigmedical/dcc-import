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
package org.icgc.dcc.imports.drug.reader;

import static org.icgc.dcc.common.core.model.FieldNames.GENE_UNIPROT_IDS;
import static org.icgc.dcc.common.core.model.FieldNames.LoaderFieldNames.GENE_ID;
import static org.icgc.dcc.imports.core.util.Jongos.createJongo;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.icgc.dcc.common.core.model.ReleaseCollection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GeneReader extends Reader {
  
  private final static String GENE_URL = BASE_URL + "genes.ldjson";
  private final static String ENSEMBLE_ID = "ensembl_gene_id";
  private final static String UNIPROT = "uniprot";
  private final static String GENE_NAME = "gene_name";
  private final static String NAME = "name";
  
  @NonNull
  private final MongoClientURI mongoUri;
  
  public GeneReader(@NonNull MongoClientURI mongoUri) {
    super(GENE_URL);
    this.mongoUri = mongoUri;
  }
  
  public List<ObjectNode> readGenes() throws IOException {
    val genes = getJson();
    return genes.readAll();
  }
  
  @SneakyThrows
  public Map<String, ObjectNode> readGeneMap() {
    log.info("Reading genes...");
    val jongo = createJongo(mongoUri);
    
    val geneCollection = jongo.getCollection(ReleaseCollection.GENE_COLLECTION.getId());
    
    val geneMap = new HashMap<String, ObjectNode>();
    getJson().forEachRemaining(gene -> {
      if (gene.get(NAME).asText().contains("HUMAN")) {
        JsonNode icgcGene = geneCollection.findOne("{#:#}", GENE_UNIPROT_IDS, gene.get(UNIPROT).asText()).as(JsonNode.class);
        if (icgcGene!=null) {
          gene.put(ENSEMBLE_ID, icgcGene.get(GENE_ID).asText());
        } else {
          log.warn("Could not find matching ICGC Gene for uniprot value: {}", gene.get(UNIPROT).asText());
          gene.put(ENSEMBLE_ID, "");
        }
        geneMap.put(gene.get(GENE_NAME).asText(), gene);
      }
    });
    
    return geneMap;
  } 
}
