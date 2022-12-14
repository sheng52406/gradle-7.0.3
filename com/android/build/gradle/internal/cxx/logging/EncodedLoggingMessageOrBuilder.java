// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: cxx_logging.proto

package com.android.build.gradle.internal.cxx.logging;

public interface EncodedLoggingMessageOrBuilder extends
    // @@protoc_insertion_point(interface_extends:EncodedLoggingMessage)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.LoggingMessage.LoggingLevel level = 1;</code>
   * @return The enum numeric value on the wire for level.
   */
  int getLevelValue();
  /**
   * <code>.LoggingMessage.LoggingLevel level = 1;</code>
   * @return The level.
   */
  com.android.build.gradle.internal.cxx.logging.LoggingMessage.LoggingLevel getLevel();

  /**
   * <code>int32 message_id = 2;</code>
   * @return The messageId.
   */
  int getMessageId();

  /**
   * <code>int32 file_id = 3;</code>
   * @return The fileId.
   */
  int getFileId();

  /**
   * <code>int32 tag_id = 4;</code>
   * @return The tagId.
   */
  int getTagId();

  /**
   * <code>int32 diagnostic_code = 5;</code>
   * @return The diagnosticCode.
   */
  int getDiagnosticCode();
}
