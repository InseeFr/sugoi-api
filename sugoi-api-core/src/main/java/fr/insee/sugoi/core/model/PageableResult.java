/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package fr.insee.sugoi.core.model;

public class PageableResult {
  private static final int TAILLE_RECHERCHE_DEFAUT = 20;

  private int size = TAILLE_RECHERCHE_DEFAUT;
  private int estimatedTotalSize = 0;
  private byte[] cookie;
  private String sortKey;
  private int offset = 1;
  private boolean pagingDisabled = false;

  public PageableResult() {}

  public PageableResult(int size, int offset) {
    this.size = size;
    this.offset = offset;
  }

  public int getSize() {
    return this.size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getEstimatedTotalSize() {
    return this.estimatedTotalSize;
  }

  public void setEstimatedTotalSize(int estimatedTotalSize) {
    this.estimatedTotalSize = estimatedTotalSize;
  }

  public byte[] getCookie() {
    return this.cookie;
  }

  public void setCookie(byte[] cookie) {
    this.cookie = cookie;
  }

  public String getSortKey() {
    return this.sortKey;
  }

  public void setSortKey(String sortKey) {
    this.sortKey = sortKey;
  }

  public int getFirst() {
    return this.offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public boolean isPagingDisabled() {
    return this.pagingDisabled;
  }

  public void setPagingDisabled(boolean pagingDisabled) {
    this.pagingDisabled = pagingDisabled;
  }
}
