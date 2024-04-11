import {boot} from 'quasar/wrappers'
import VueDraggable from 'vue-draggable'

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli/boot-files
export default boot(async ({app}) => {
  app.use(VueDraggable)
})
