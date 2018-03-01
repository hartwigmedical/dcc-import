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
package org.icgc.dcc.imports.project;

import static org.icgc.dcc.common.core.util.Formats.formatCount;

import java.io.IOException;

import org.icgc.dcc.common.client.api.cgp.CGPClient;
import org.icgc.dcc.imports.core.SourceImporter;
import org.icgc.dcc.imports.core.model.ImportSource;
import org.icgc.dcc.imports.project.model.Project;
import org.icgc.dcc.imports.project.reader.ProjectReader;
import org.icgc.dcc.imports.project.writer.ProjectWriter;

import com.google.common.base.Stopwatch;
import com.mongodb.MongoClientURI;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProjectImporter implements SourceImporter {

  /**
   * Configuration
   */
  @NonNull
  private final MongoClientURI mongoUri;

  /**
   * Dependencies.
   */

  @Override
  public ImportSource getSource() {
    return ImportSource.PROJECTS;
  }

  @Override
  @SneakyThrows
  public void execute() {
    val watch = Stopwatch.createStarted();

    val projects = readProjects();

    log.info("Writing {} projects to {}...", formatCount(projects), mongoUri);
    writeProjects(projects);

    log.info("Finished writing {} of {} projects in {}",
        formatCount(projects), formatCount(projects), watch);
  }

  private Iterable<Project> readProjects() {
    return new ProjectReader().read();
  }

  private void writeProjects(Iterable<Project> specifiedProjects) throws IOException {
    @Cleanup
    val writer = new ProjectWriter(mongoUri);
    writer.writeValue(specifiedProjects);
  }

}
