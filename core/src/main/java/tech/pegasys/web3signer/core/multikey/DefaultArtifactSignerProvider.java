/*
 * Copyright 2020 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package tech.pegasys.web3signer.core.multikey;

import tech.pegasys.web3signer.core.signing.ArtifactSigner;
import tech.pegasys.web3signer.core.signing.ArtifactSignerProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DefaultArtifactSignerProvider implements ArtifactSignerProvider {

  private static final Logger LOG = LogManager.getLogger();
  private final Map<String, ArtifactSigner> signers;

  private DefaultArtifactSignerProvider(final Map<String, ArtifactSigner> signers) {
    this.signers = signers;
  }

  public static DefaultArtifactSignerProvider create(final Collection<ArtifactSigner> signers) {
    final Map<String, ArtifactSigner> signerMap =
        signers
            .parallelStream()
            .collect(
                Collectors.toMap(
                    ArtifactSigner::getIdentifier,
                    Function.identity(),
                    (signer1, signer2) -> {
                      LOG.warn("Duplicate keys were found.");
                      return signer1;
                    }));
    return new DefaultArtifactSignerProvider(signerMap);
  }

  @Override
  public Optional<ArtifactSigner> getSigner(final String identifier) {
    final Optional<ArtifactSigner> result = Optional.ofNullable(signers.get(identifier));

    if (result.isEmpty()) {
      LOG.error("No signer was loaded matching identifier '{}'", identifier);
    }
    return result;
  }

  @Override
  public Set<String> availableIdentifiers() {
    return signers.keySet().parallelStream().collect(Collectors.toSet());
  }
}
