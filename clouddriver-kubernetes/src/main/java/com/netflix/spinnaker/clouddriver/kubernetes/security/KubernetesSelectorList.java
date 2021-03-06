/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netflix.spinnaker.clouddriver.kubernetes.security;

import com.google.common.collect.ImmutableList;
import com.netflix.spinnaker.clouddriver.kubernetes.security.KubernetesSelector.Kind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class KubernetesSelectorList {
  private final List<KubernetesSelector> selectors = new ArrayList<>();

  public KubernetesSelectorList() {}

  private KubernetesSelectorList(List<KubernetesSelector> selectors) {
    this.selectors.addAll(selectors);
  }

  public KubernetesSelectorList(KubernetesSelector... selectors) {
    this.selectors.addAll(Arrays.asList(selectors));
  }

  public boolean isNotEmpty() {
    return !selectors.isEmpty();
  }

  public KubernetesSelectorList addSelector(KubernetesSelector selector) {
    selectors.add(selector);
    return this;
  }

  public KubernetesSelectorList addSelectors(KubernetesSelectorList selectors) {
    this.selectors.addAll(selectors.selectors);
    return this;
  }

  public boolean isEmpty() {
    return selectors.isEmpty();
  }

  @Override
  public String toString() {
    return selectors.stream().map(KubernetesSelector::toString).collect(Collectors.joining(","));
  }

  public static KubernetesSelectorList fromMatchLabels(Map<String, String> matchLabels) {
    return new KubernetesSelectorList(
        matchLabels.entrySet().stream()
            .map(
                kv ->
                    new KubernetesSelector(
                        Kind.EQUALS, kv.getKey(), ImmutableList.of(kv.getValue())))
            .collect(Collectors.toList()));
  }

  public static KubernetesSelectorList fromMatchExpressions(
      List<MatchExpression> matchExpressions) {
    return new KubernetesSelectorList(
        matchExpressions.stream()
            .map(KubernetesSelectorList::fromMatchExpression)
            .collect(Collectors.toList()));
  }

  private static KubernetesSelector fromMatchExpression(MatchExpression matchExpression) {
    KubernetesSelector.Kind kind;
    switch (matchExpression.getOperator()) {
      case In:
        kind = KubernetesSelector.Kind.CONTAINS;
        break;
      case NotIn:
        kind = KubernetesSelector.Kind.NOT_CONTAINS;
        break;
      case Exists:
        kind = KubernetesSelector.Kind.EXISTS;
        break;
      case DoesNotExist:
        kind = KubernetesSelector.Kind.NOT_EXISTS;
        break;
      default:
        throw new IllegalArgumentException("Unknown operator: " + matchExpression.getOperator());
    }

    return new KubernetesSelector(kind, matchExpression.getKey(), matchExpression.getValues());
  }
}
