/*
 * Copyright (c) 2018
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pedro Cuadra - pjcuadra@gmail.com
 */
package org.eclipse.che.kuksa.ide.macro;

import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.macro.Macro;
import org.eclipse.che.ide.api.macro.MacroRegistry;
import org.eclipse.che.ide.api.workspace.event.WorkspaceRunningEvent;
import org.eclipse.che.ide.api.workspace.event.WorkspaceStoppedEvent;
import org.eclipse.che.ide.bootstrap.BasicIDEInitializedEvent;

/** @author Pedro Cuadra */
@Singleton
public class RemoteTargetMacroRegistrar {

  private final Provider<MacroRegistry> macroRegistryProvider;

  private Set<Macro> macros = new HashSet<>();

  @Inject
  public RemoteTargetMacroRegistrar(
      EventBus eventBus,
      Provider<MacroRegistry> macroRegistryProvider,
      AppContext appContext,
      RemoteTargetHostnameMacro remoteTargetHostnameMacro,
      RemoteTargetUserMacro remoteTargetUserMacro) {
    this.macroRegistryProvider = macroRegistryProvider;

    macros.add(remoteTargetHostnameMacro);
    macros.add(remoteTargetUserMacro);

    eventBus.addHandler(
        BasicIDEInitializedEvent.TYPE,
        e -> {
          if (appContext.getWorkspace().getStatus() == RUNNING) {
            registerMacros();
          }
        });

    eventBus.addHandler(WorkspaceRunningEvent.TYPE, e -> registerMacros());

    eventBus.addHandler(
        WorkspaceStoppedEvent.TYPE,
        e -> {
          if (macros != null) {
            macros.forEach(macro -> macroRegistryProvider.get().unregister(macro));
            macros.clear();
          }
        });
  }

  private void registerMacros() {
    macroRegistryProvider.get().register(macros);
  }
}
