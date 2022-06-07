// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: CrossLanMessage.proto

package com.wg.crosslan.common.protocol.proto;

public interface CrossLanMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:CrossLanMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>optional uint32 id = 1;</code>
   * @return Whether the id field is set.
   */
  boolean hasId();
  /**
   * <code>optional uint32 id = 1;</code>
   * @return The id.
   */
  int getId();

  /**
   * <code>map&lt;string, string&gt; meta_data = 2;</code>
   */
  int getMetaDataCount();
  /**
   * <code>map&lt;string, string&gt; meta_data = 2;</code>
   */
  boolean containsMetaData(
      java.lang.String key);
  /**
   * Use {@link #getMetaDataMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.String, java.lang.String>
  getMetaData();
  /**
   * <code>map&lt;string, string&gt; meta_data = 2;</code>
   */
  java.util.Map<java.lang.String, java.lang.String>
  getMetaDataMap();
  /**
   * <code>map&lt;string, string&gt; meta_data = 2;</code>
   */

  java.lang.String getMetaDataOrDefault(
      java.lang.String key,
      java.lang.String defaultValue);
  /**
   * <code>map&lt;string, string&gt; meta_data = 2;</code>
   */

  java.lang.String getMetaDataOrThrow(
      java.lang.String key);

  /**
   * <code>.Type type = 3;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <code>.Type type = 3;</code>
   * @return The type.
   */
  com.wg.crosslan.common.protocol.proto.Type getType();

  /**
   * <code>optional bytes data = 4;</code>
   * @return Whether the data field is set.
   */
  boolean hasData();
  /**
   * <code>optional bytes data = 4;</code>
   * @return The data.
   */
  com.google.protobuf.ByteString getData();

  /**
   * <code>optional bool is_success = 5;</code>
   * @return Whether the isSuccess field is set.
   */
  boolean hasIsSuccess();
  /**
   * <code>optional bool is_success = 5;</code>
   * @return The isSuccess.
   */
  boolean getIsSuccess();

  /**
   * <code>optional string channel_id = 6;</code>
   * @return Whether the channelId field is set.
   */
  boolean hasChannelId();
  /**
   * <code>optional string channel_id = 6;</code>
   * @return The channelId.
   */
  java.lang.String getChannelId();
  /**
   * <code>optional string channel_id = 6;</code>
   * @return The bytes for channelId.
   */
  com.google.protobuf.ByteString
      getChannelIdBytes();
}