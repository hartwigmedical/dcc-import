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
package org.icgc.dcc.imports.core.util;

import static org.icgc.dcc.imports.core.util.Jongos.createJongo;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

import org.icgc.dcc.common.core.model.ReleaseCollection;
import org.jongo.Jongo;
import org.jongo.MongoCollection;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.MongoClientURI;

import lombok.NonNull;
import lombok.val;

public abstract class AbstractJongoComponent implements Closeable {

  /**
   * Configuration.
   */
  protected final MongoClientURI mongoUri;

  /**
   * Dependencies.
   */
  protected final Jongo jongo;

  public AbstractJongoComponent(@NonNull MongoClientURI mongoUri) {
    this.mongoUri = mongoUri;
    this.jongo = createJongo(mongoUri);
  }

  @Override
  public void close() throws IOException {
    jongo.getDatabase().getMongo().close();
  }

  protected MongoCollection getCollection(ReleaseCollection releaseCollection) {
    return jongo.getCollection(releaseCollection.getId());
  }

  @NonNull
  protected void eachDocument(ReleaseCollection collection, Consumer<ObjectNode> consumer) {
    val documents = getCollection(collection);
    for (val document : documents.find().as(ObjectNode.class)) {
      consumer.accept(document);
    }
  }

}
