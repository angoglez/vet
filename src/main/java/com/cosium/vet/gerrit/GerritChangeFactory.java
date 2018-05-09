package com.cosium.vet.gerrit;

import com.cosium.vet.git.BranchShortName;

/**
 * Created on 08/05/18.
 *
 * @author Reda.Housni-Alaoui
 */
public interface GerritChangeFactory {

  /**
   * @param targetBranch The target branch of the change
   * @param changeNumericId The numeric id of the change
   * @return An object describing an existing Gerrit Change
   */
  GerritChange build(BranchShortName targetBranch, ChangeNumericId changeNumericId);
}
