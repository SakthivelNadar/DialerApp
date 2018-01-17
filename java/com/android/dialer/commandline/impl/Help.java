/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.dialer.commandline.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import com.android.dialer.commandline.Command;
import com.android.dialer.commandline.CommandLineComponent;
import com.android.dialer.inject.ApplicationContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;

/** List available commands */
public class Help implements Command {

  private final Context context;

  @Inject
  Help(@ApplicationContext Context context) {
    this.context = context;
  }

  @Override
  public ListenableFuture<String> run(ImmutableList<String> args) {
    boolean showHidden = args.contains("--showHidden");

    StringBuilder stringBuilder = new StringBuilder();
    ImmutableMap<String, Command> commands =
        CommandLineComponent.get(context).commandSupplier().get();
    stringBuilder
        .append(runOrThrow(commands.get("version")))
        .append("\n")
        .append("\n")
        .append("usage: <command> [args...]\n")
        .append("\n")
        .append("<command>\n");

    for (Entry<String, Command> entry : commands.entrySet()) {
      String description = entry.getValue().getShortDescription();
      if (!showHidden && description.startsWith("@hide ")) {
        continue;
      }
      stringBuilder
          .append("\t")
          .append(entry.getKey())
          .append("\t")
          .append(description)
          .append("\n");
    }

    return Futures.immediateFuture(stringBuilder.toString());
  }

  private static String runOrThrow(Command command) {
    try {
      return command.run(ImmutableList.of()).get();
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new RuntimeException(e);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  @NonNull
  @Override
  public String getShortDescription() {
    return "Print this message";
  }
}
