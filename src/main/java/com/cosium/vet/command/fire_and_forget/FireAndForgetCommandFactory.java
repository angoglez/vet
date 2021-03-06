package com.cosium.vet.command.fire_and_forget;

import com.cosium.vet.gerrit.CodeReviewVote;

/**
 * Created on 09/05/18.
 *
 * @author Reda.Housni-Alaoui
 */
public interface FireAndForgetCommandFactory {

  /**
   * @param force True to force the command execution without prompt
   * @param codeReviewVote The code review vote to apply
   * @return A new command
   */
  FireAndForgetCommand build(Boolean force, CodeReviewVote codeReviewVote);
}
