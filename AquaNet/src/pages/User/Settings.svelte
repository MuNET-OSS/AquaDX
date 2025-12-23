<!-- Svelte 4.2.11 -->

<script lang="ts">
  import { slide, fade } from "svelte/transition";
  import type { AquaNetUser } from "../../libs/generalTypes";
  import { CARD, USER } from "../../libs/sdk";
  import StatusOverlays from "../../components/StatusOverlays.svelte";
  import Icon from "@iconify/svelte";
  import { t, ts } from "../../libs/i18n";
  import { FADE_IN, FADE_OUT } from "../../libs/config";
  import UserBox from "../../components/settings/ChuniSettings.svelte";
  import Mai2Settings from "../../components/settings/Mai2Settings.svelte";
  import WaccaSettings from "../../components/settings/WaccaSettings.svelte";
  import GeneralGameSettings from "../../components/settings/GeneralGameSettings.svelte";
  import OngekiSettings from "../../components/settings/OngekiSettings.svelte";

  USER.ensureLoggedIn()

  let me: AquaNetUser;
  let error: string;
  let submitting = ""
  let tab = 0
  let tabs = ['profile']

  const profileFields = [
    [ 'displayName', t('settings.profile.name') ],
    // [ 'username', t('settings.profile.username') ],
    // [ 'password', t('settings.profile.password') ],
    /* Neither of these did anything of importance
    [ 'country', t('settings.profile.country') ],
    [ 'profileLocation', t('settings.profile.location') ],*/
    [ 'profileBio', t('settings.profile.bio') ],
  ] as const

  // Fetch user data
  const getMe = () => USER.me().then((m) => {
    me = m

    CARD.userGames(m.username).then(games => {
      tabs = [
        ...tabs,
        ...['chu3', 'mai2','wacca', 'ongeki'].filter(v => games[v as keyof typeof games]), // :xdx:
        'global'
      ]
    })
  }).catch(e => error = e.message)
  getMe()

  let changed: string[] = []

  function submit(field: string, value: string) {
    if (submitting) return
    submitting = field

    USER.setting(field, value).then(() => {
      changed = changed.filter(c => c !== field)
    }).catch(e => error = e.message).finally(() => submitting = "")
  }

  function logOut() {
    localStorage.removeItem("token");
    location.href = "/";
  }

  const passwordAction = (node: HTMLInputElement, whether: boolean) => {
    if (whether) node.type = 'password'
  }
</script>

<main class="content">
  <div class="outer-title-options">
    <h2>{t('settings.title')}</h2>
    <nav>
      {#each tabs as tabName, i}
        <div transition:slide={{axis: 'x'}} class:active={tab === i}
             on:click={() => tab = i} on:keydown={e => e.key === 'Enter' && (tab = i)}
             role="button" tabindex="0">
          {ts(`settings.tabs.${tabName}`)}
        </div>
      {/each}
    </nav>
  </div>

  {#if tab === 0 && me}
    <!-- Tab 0: Profile settings -->
    <div out:fade={FADE_OUT} in:fade={FADE_IN} class="fields">
      {#each profileFields as [field, name], i (field)}
        <div class="field">
          <label for={field}>{name}</label>
          <div>
            {#if field == "profileBio"}
             <textarea id={field} bind:value={me[field]} on:input={() => changed = [...changed, field]} maxlength=255 placeholder={t('settings.profile.unset')}></textarea>
            {:else}
              <input id={field} type="text"
                bind:value={me[field]} on:input={() => changed = [...changed, field]}
                placeholder={t('settings.profile.unset')}/>
            {/if}

            {#if changed.includes(field) && me[field]}
              <button transition:slide={{axis: 'x'}} on:click={() => submit(field, me[field])}>
                {#if submitting === field}
                  <Icon icon="line-md:loading-twotone-loop" />
                {:else}
                  {t('settings.profile.save')}
                {/if}
              </button>
            {/if}
          </div>
        </div>
      {/each}
      <div class="field m-t">
        <div class="bool">
          <input id="optOutOfLeaderboard" type="checkbox" bind:checked={me.optOutOfLeaderboard}
                 on:change={() => submit('optOutOfLeaderboard', me.optOutOfLeaderboard.toString())}/>
          <label for="optOutOfLeaderboard">
            <span class="name">{ts(`settings.fields.optOutOfLeaderboard.name`)}</span>
            <span class="desc">{ts(`settings.fields.optOutOfLeaderboard.desc`)}</span>
          </label>
        </div>
      </div>
      <div class="field m-t">
        <div>
          <button on:click={logOut}>{ts(`settings.profile.logout`)}</button>
        </div>
      </div>
    </div>
  {:else if tabs[tab] === 'chu3'}
    <!-- Userbox settings -->
    <UserBox />
  {:else if tabs[tab] === 'mai2'}
    <Mai2Settings username={me.username} />
  {:else if tabs[tab] === 'wacca'}
    <WaccaSettings />
  {:else if tabs[tab] === 'ongeki'}
    <OngekiSettings />
  {:else if tabs[tab] === 'global'}
    <GeneralGameSettings />
  {/if}

  <StatusOverlays {error} loading={!me || !!submitting} />
</main>

<style lang="sass">
  @use "../../vars"

  .fields
    display: flex
    flex-direction: column
    gap: 12px

  .bool
    display: flex
    align-items: center
    gap: 1rem

    label
      display: flex
      flex-direction: column

      .desc
        opacity: 0.6

  .field
    display: flex
    flex-direction: column

    label
      max-width: max-content

    > div:not(.bool)
      display: flex
      align-items: center
      gap: 1rem
      margin-top: 0.5rem

      > input, > textarea
        flex: 1

    img
      max-width: 100px
      max-height: 100px
      border-radius: vars.$border-radius
      object-fit: cover
      aspect-ratio: 1



  .cropper-container
    position: relative
    width: 400px
    aspect-ratio: 1
</style>
