<script lang="ts">
  import { USER } from "../libs/sdk.js";

  USER.ensureLoggedIn();

</script>

<style lang="sass">
  @use "../vars"

  .tabs
    display: flex
    gap: 1rem

    div
      &.active
        color: vars.$c-main

  h3
    font-size: 1.3rem
    margin: 0

  .action-cards
    display: flex
    flex-direction: column
    gap: 1rem
</style>
