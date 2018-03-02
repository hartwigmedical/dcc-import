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
package org.icgc.dcc.imports.go.util;

import static org.icgc.dcc.imports.core.util.Genes.getGeneUniprotIds;
import static org.icgc.dcc.imports.geneset.model.GeneSetAnnotation.DIRECT;
import static org.icgc.dcc.imports.geneset.model.GeneSetAnnotation.INFERRED;
import static org.icgc.dcc.imports.geneset.model.GeneSetType.GO_TERM;
import static org.icgc.dcc.imports.go.util.GoAssociationIndexer.indexGoId;
import static org.icgc.dcc.imports.go.util.GoAssociationIndexer.indexUniProtId;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.icgc.dcc.imports.geneset.model.gene.GeneGeneSet;
import org.icgc.dcc.imports.geneset.model.go.GoInferredTreeNode;
import org.icgc.dcc.imports.go.model.GoAssociation;
import org.icgc.dcc.imports.go.model.GoModel;
import org.icgc.dcc.imports.go.model.GoTerm;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class GoGeneGeneSetsBuilder {

    /**
     * Data.
     */
    @NonNull
    private final Map<String, List<GoInferredTreeNode>> inferredTrees;

    /**
     * Indexed data.
     */
    @NonNull
    private final Map<String, GoTerm> goIdTermsIndex;
    @NonNull
    private final Multimap<String, GoAssociation> uniprotIdAssociationsIndex;
    @NonNull
    private final Multimap<String, GoAssociation> goIdAssociationsIndex;

    public GoGeneGeneSetsBuilder(@NonNull GoModel model) {
        this.inferredTrees = model.getInferredTrees();

        log.info("Indexing GO model...");
        this.goIdTermsIndex = GoTermIndexer.indexGoId(model.getTerms());
        this.uniprotIdAssociationsIndex = indexUniProtId(model.getAssociations());
        this.goIdAssociationsIndex = indexGoId(model.getAssociations());
    }

    public Set<GeneGeneSet> build(@NonNull ObjectNode gene) {
        val geneSets = Sets.<GeneGeneSet>newHashSet();
        val geneUniprotIds = getGeneUniprotIds(gene);
        log.debug("Uniprots IDs: {}", geneUniprotIds);

        for (val uniprotId : geneUniprotIds) {
            buildUniprotInferredGeneSets(geneSets, geneUniprotIds, uniprotId);
        }

        return geneSets;
    }

    private void buildUniprotInferredGeneSets(Set<GeneGeneSet> geneSets, Set<String> geneUniprotIds, String uniprotId) {
        val uniprotAssociations = uniprotIdAssociationsIndex.get(uniprotId);

        for (val uniprotAssociation : uniprotAssociations) {
            buildUniprotTermInferredGeneSets(geneSets, geneUniprotIds, uniprotAssociation.getKey().getGoId());
        }
    }

    private void buildUniprotTermInferredGeneSets(Set<GeneGeneSet> geneSets, Set<String> geneUniprotIds, String uniprotGoId) {
        val uniprotTermInferredTree = inferredTrees.get(uniprotGoId);
        if (uniprotTermInferredTree != null) {
            // Walk the tree
            for (val uniprotTermInferredTreeNode : uniprotTermInferredTree) {
                val uniprotTermInferredTreeTerm = goIdTermsIndex.get(uniprotTermInferredTreeNode.getId());
                if (uniprotTermInferredTreeTerm == null) {
                    log.error("uniprotTermInferredTreeTerm for Inferred tree node {} was null, proceed with caution",
                            uniprotTermInferredTreeNode.getId());
                } else {
                    val geneSet = buildUniprotTermInferredTreeNodeGeneSet(geneUniprotIds, uniprotTermInferredTreeTerm);
                    geneSets.add(geneSet);
                }
            }
        }
    }

    private GeneGeneSet buildUniprotTermInferredTreeNodeGeneSet(Set<String> geneUniprotIds, GoTerm uniprotTermInferredTreeTerm) {
        val uniprotTermInferredTreeTermAssociations = goIdAssociationsIndex.get(uniprotTermInferredTreeTerm.getId());
        boolean direct = false;
        val uniqueQualifiers = Sets.<String>newHashSet();
        for (val uniprotTermInferredTreeTermAssociation : uniprotTermInferredTreeTermAssociations) {
            val inferredUniprotId = uniprotTermInferredTreeTermAssociation.getKey().getUniProtId();
            if (geneUniprotIds.contains(inferredUniprotId)) {
                direct = true;
                val qualifiers = uniprotTermInferredTreeTermAssociation.getQualifiers();

                if (qualifiers == null || qualifiers.isEmpty()) {
                    uniqueQualifiers.add(null);
                } else {
                    uniqueQualifiers.addAll(qualifiers);
                }
            }
        }

        return GeneGeneSet.builder()
                .id(uniprotTermInferredTreeTerm.getId())
                .name(uniprotTermInferredTreeTerm.getName())
                .type(GO_TERM)
                .annotation(direct ? DIRECT : INFERRED)
                .qualifiers(direct ? uniqueQualifiers : null)
                .build();
    }

}
