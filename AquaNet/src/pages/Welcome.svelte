<script lang="ts">
  import { USER } from "../libs/sdk";

  if (USER.isLoggedIn()) {
    window.location.href = "/home"
  }

</script>

<main id="home" class="no-margin">
  <div>
    <h1 id="title">AquaNet</h1>
  </div>

  <div class="light-pollution">
    <div class="l1"></div>
    <div class="l2"></div>
    <div class="l3"></div>
  </div>
</main>

<style lang="sass">
  @use "../vars"

  .login-form
    display: flex
    flex-direction: column
    gap: 8px
    width: calc(100% - 12px)
    max-width: 300px

    div.clickable
      display: flex
      align-items: center

  #home
    color: vars.$c-main
    position: relative
    width: 100%
    height: 100%
    padding-left: 100px
    overflow: hidden
    background-color: black

    box-sizing: border-box

    display: flex
    flex-direction: column
    justify-content: center

    margin-top: -(vars.$nav-height)

    // Content container
    > div
      display: flex
      flex-direction: column
      align-items: flex-start
      width: max-content

      // Switching state container
      > div
        transition: vars.$transition

    #title
      font-family: Quicksand, vars.$font
      user-select: none

      // Gap between text characters
      letter-spacing: 0.2em
      margin-top: 0
      margin-bottom: 32px
      opacity: 0.9

    .btn-group
      display: flex
      gap: 8px

    .light-pollution
      pointer-events: none
      opacity: 0.8

      > div
        position: absolute
        z-index: 1

      .l1
        left: -560px
        top: 90px
        height: 1130px
        width: 1500px
        $color: rgb(158, 110, 230)
        background: radial-gradient(50% 50% at 50% 50%, rgba($color, 0.28) 0%, rgba(0,0,0,0) 100%)

      .l2
        left: -200px
        top: 560px
        height: 1200px
        width: 1500px
        $color: rgb(92, 195, 250)
        background: radial-gradient(50% 50% at 50% 50%, rgba($color, 0.28) 0%, rgba(0,0,0,0) 100%)

      .l3
        left: -600px
        opacity: 0.7
        top: -630px
        width: 1500px
        height: 1000px
        $color: rgb(230, 110, 156)
        background: radial-gradient(50% 50% at 50% 50%, rgba($color, 0.28) 0%, rgba(0,0,0,0) 100%)

    @media (max-width: 500px)
      align-items: center
      padding-left: 0
</style>
