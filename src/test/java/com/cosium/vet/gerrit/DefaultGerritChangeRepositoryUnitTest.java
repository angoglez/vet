package com.cosium.vet.gerrit;

import com.cosium.vet.gerrit.config.GerritConfiguration;
import com.cosium.vet.gerrit.config.GerritConfigurationRepository;
import com.cosium.vet.git.BranchShortName;
import com.cosium.vet.git.GitClient;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Created on 23/02/18.
 *
 * @author Reda.Housni-Alaoui
 */
public class DefaultGerritChangeRepositoryUnitTest {

  private AtomicReference<GerritConfiguration> lastSavedConfiguration;

  private GerritConfiguration gerritConfiguration;
  private ChangeFactory changeFactory;
  private AlterableChangeFactory alterableChangeFactory;
  private PatchsetRepository patchSetRepository;
  private GitClient git;

  private ChangeRepository tested;

  @Before
  public void before() {
    lastSavedConfiguration = new AtomicReference<>();

    GerritConfigurationRepository configurationRepository =
        mock(GerritConfigurationRepository.class);
    gerritConfiguration = mock(GerritConfiguration.class);
    when(configurationRepository.read()).thenReturn(gerritConfiguration);
    when(configurationRepository.readAndWrite(any()))
        .thenAnswer(
            invocation -> {
              Function<GerritConfiguration, ?> func = invocation.getArgument(0);
              Object res = func.apply(gerritConfiguration);
              lastSavedConfiguration.set(gerritConfiguration);
              return res;
            });

    changeFactory = mock(ChangeFactory.class);
    alterableChangeFactory = mock(AlterableChangeFactory.class);
    patchSetRepository = mock(PatchsetRepository.class);
    git = mock(GitClient.class);
    tested =
        new DefaultChangeRepository(
            configurationRepository,
            changeFactory,
            alterableChangeFactory,
            patchSetRepository,
            git);
  }

  @Test
  public void GIVEN_no_current_change_WHEN_get_change_THEN_it_should_return_empty() {
    assertThat(tested.getTrackedChange()).isEmpty();
  }

  @Test
  public void
      GIVEN_tracked_change_1234_for_feature_WHEN_get_tracked_change_THEN_it_should_return_matching_change() {
    ChangeNumericId _1234 = ChangeNumericId.of(1234);
    BranchShortName feature = BranchShortName.of("feature");
    when(gerritConfiguration.getTrackedChangeNumericId()).thenReturn(Optional.of(_1234));
    when(gerritConfiguration.getTrackedChangeTargetBranch()).thenReturn(Optional.of(feature));

    AlterableChange gerritChange = mock(AlterableChange.class);
    when(alterableChangeFactory.build(feature, _1234)).thenReturn(gerritChange);

    assertThat(tested.getTrackedChange()).contains(gerritChange);
  }

  @Test
  public void
      WHEN_track_change_1234_on_master_THEN_it_should_store_1234_and_master_in_configuration() {
    ChangeNumericId _1234 = ChangeNumericId.of(1234);
    tested.trackChange(_1234, BranchShortName.MASTER);
    assertThat(lastSavedConfiguration.get()).isNotNull();

    verify(lastSavedConfiguration.get()).setTrackedChangeNumericId(_1234);
    verify(lastSavedConfiguration.get()).setTrackedChangeTargetBranch(BranchShortName.MASTER);
  }

  @Test
  public void WHEN_untrack_THEN_it_clears_the_gerrit_conf() {
    tested.untrack();

    verify(gerritConfiguration).setTrackedChangeTargetBranch(null);
    verify(gerritConfiguration).setTrackedChangeNumericId(null);
  }

  @Test
  public void GIVEN_no_patchset_for_change_1234_WHEN_findChange_1234_THEN_it_returns_empty() {
    assertThat(tested.findChange(ChangeNumericId.of(1234))).isEmpty();
  }

  @Test
  public void GIVEN_existing_patchset_for_change_1234_WHEN_findchange_1234_THEN_it_returns_it() {
    Change expectedChange = mock(Change.class);
    ChangeNumericId numericId = ChangeNumericId.of(1234);
    when(patchSetRepository.findLatestPatchset(numericId))
        .thenReturn(Optional.of(mock(Patchset.class)));
    when(changeFactory.build(numericId)).thenReturn(expectedChange);

    assertThat(tested.findChange(numericId)).contains(expectedChange);
  }
}
