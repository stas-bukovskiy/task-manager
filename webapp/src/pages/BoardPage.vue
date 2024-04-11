<template>
  <q-page>
    <div class="q-pl-md q-pr-md q-mr-md">
      <div class="bg-grey-3 row justify-between q-pr-md">
        <div class="row items-center">
          <q-avatar font-size="52px" icon="assessment" size="60px">
          </q-avatar>
          <h4 class="q-ma-xs">{{ board.title }}</h4>
        </div>
        <div v-if="board.ownerId === userId" class="row items-center">
          <q-btn class="q-mr-sm" color="primary" icon="edit" label="Edit" @click="openEditBoardModal"/>
          <q-btn color="secondary" icon="delete" label="Delete" outlined @click="openDeleteBoardModal"/>
        </div>

        <q-dialog v-model="editModal" persistent>
          <EditBoardModal v-model:id="board.id"/>
        </q-dialog>
        <q-dialog v-model="deleteModal">
          <DeleteBoardModal v-model:id="board.id" v-model:title="board.title"/>
        </q-dialog>
      </div>
    </div>
    <KanbanBoard v-model:id="board.id"/>
  </q-page>
</template>

<script>
import KanbanBoard from 'components/KanbanBoard.vue';
import {mapActions, mapGetters} from "vuex";
import EditBoardModal from "components/EditBoardModal.vue";
import DeleteBoardModal from "components/DeleteBoardModal.vue";

export default {
  components: {
    DeleteBoardModal,
    EditBoardModal,
    KanbanBoard,
  },
  data() {
    return {
      board: {
        id: this.$route.params.id.toString(),
        title: '',
        ownerId: '',
      },
      userId: '',
      editModal: false,
      deleteModal: false,
    };
  },
  computed: {
    ...mapGetters('boards', ['getBoard']),
  },
  methods: {
    ...mapActions('boards', ['fetchBoard']),
    ...mapGetters('auth', ['getUser']),
    openEditBoardModal() {
      this.editModal = true;
    },
    openDeleteBoardModal() {
      this.deleteModal = true;
    },
    fetchAndUpdateBoard() {
      this.fetchBoard(this.board.id).then(() => {
        const item = this.getBoard(this.board.id);
        console.log("board", this.board.id, item)
        if (item) {
          this.board = {
            id: item.id,
            title: item.title,
            description: item.description,
            ownerId: item.ownerId
          };
        }
      });
    }
  },
  mounted() {
    this.fetchBoard(this.board.id).then(() => {
      const item = this.getBoard(this.board.id);
      console.log("board", this.board.id, item)
      if (item) {
        this.board = {
          id: item.id,
          title: item.title,
          description: item.description,
          ownerId: item.ownerId
        };
      }
    });

    this.userId = this.getUser().id;
  },
  watch: {
    '$route.params.id': {
      immediate: true,
      handler(newId) {
        this.board.id = newId.toString();
        this.fetchAndUpdateBoard();
      }
    }
  }
};
</script>
