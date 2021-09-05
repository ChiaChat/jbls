/*
 * Copyright 2019 ConsenSys AG.
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

package org.chiachat.jbls;

import static java.util.Objects.isNull;

import com.google.common.base.Suppliers;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.InvalidSSZTypeException;
import org.apache.tuweni.ssz.SSZ;
import org.chiachat.jbls.impl.DeserializeException;
import org.chiachat.jbls.impl.Signature;

public class BLSSignature {

  public static final int SSZ_BLS_SIGNATURE_SIZE = BLSConstants.BLS_SIGNATURE_SIZE;
  private static final Bytes INFINITY_BYTES =
      Bytes.fromHexString(
          "0x"
              + "c000000000000000000000000000000000000000000000000000000000000000"
              + "0000000000000000000000000000000000000000000000000000000000000000"
              + "0000000000000000000000000000000000000000000000000000000000000000");

  /**
   * Creates an empty signature (all zero bytes). Note that this is not a valid signature.
   *
   * @return the empty signature
   */
  public static BLSSignature empty() {
    return BLSSignature.fromBytesCompressed(Bytes.wrap(new byte[SSZ_BLS_SIGNATURE_SIZE]));
  }

  public static BLSSignature infinity() {
    return BLSSignature.fromBytesCompressed(INFINITY_BYTES);
  }

  public static BLSSignature fromBytesCompressed(Bytes bytes) {
    return new BLSSignature(bytes);
  }

  public static BLSSignature fromSSZBytes(Bytes bytes) {
    try {
      return SSZ.decode(
          bytes, reader -> new BLSSignature(reader.readFixedBytes(SSZ_BLS_SIGNATURE_SIZE)));
    } catch (InvalidSSZTypeException e) {
      throw new DeserializeException("Failed to create signature from SSZ.");
    }
  }

  // Sometimes we are dealing with random, invalid signature points, e.g. when testing.
  // Let's only interpret the raw data into a point when necessary to do so.
  // And vice versa while aggregating we are dealing with points only so let's
  // convert point to raw data when necessary to do so.
  private final Supplier<Signature> signature;
  private final Supplier<Bytes> bytesCompressed;

  /**
   * Construct from an implementation-specific Signature object.
   *
   * @param signature An implementation-specific Signature
   */
  BLSSignature(Signature signature) {
    this(() -> signature, Suppliers.memoize(signature::toBytesCompressed));
  }

  BLSSignature(Bytes signatureBytes) {
    this(
        Suppliers.memoize(() -> BLS.getBlsImpl().signatureFromCompressed(signatureBytes)),
        () -> signatureBytes);
  }

  private BLSSignature(Supplier<Signature> signature, Supplier<Bytes> bytesCompressed) {
    this.signature = signature;
    this.bytesCompressed = bytesCompressed;
  }

  /**
   * Returns the SSZ serialization of the <em>compressed</em> form of the signature.
   *
   * @return the serialization of the compressed form of the signature.
   */
  public Bytes toSSZBytes() {
    return SSZ.encode(
        writer -> {
          writer.writeFixedBytes(bytesCompressed.get());
        });
  }

  public Bytes toBytesCompressed() {
    return bytesCompressed.get();
  }

  Signature getSignature() {
    try {
      return signature.get();
    } catch (final IllegalArgumentException e) {
      throw new DeserializeException(e.getMessage());
    }
  }

  public boolean isInfinity() {
    try {
      Signature signature = getSignature();
      return signature.isInfinity();
    } catch (final DeserializeException e) {
      return false;
    }
  }

  public boolean isValid() {
    try {
      return getSignature().isValid();
    } catch (final DeserializeException e) {
      return false;
    }
  }

  @Override
  public String toString() {
    return toBytesCompressed().toString();
  }

  @Override
  public int hashCode() {
    return toBytesCompressed().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (isNull(obj)) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BLSSignature)) {
      return false;
    }
    BLSSignature other = (BLSSignature) obj;
    return Objects.equals(toBytesCompressed(), other.toBytesCompressed());
  }
}
