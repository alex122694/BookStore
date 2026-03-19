<script setup>
defineProps({
    genres: {
        type: Array,
        required: true
    },
    selectedGenreId: {
        type: [String, Number],
        required: true
    }
});

const emit = defineEmits(['select']);

const selectGenre = (id) => {
    emit('select', id);
};
</script>

<template>
    <v-card class="rounded-lg elevation-2 border-opacity-75 sticky-sidebar" color="white">
        <v-list class="py-2" density="compact">
            <v-list-subheader class="text-h6 font-weight-bold text-primary mb-2">書籍分類</v-list-subheader>

            <!-- 全部書籍 -->
            <v-list-item value="all" active-color="primary" :active="selectedGenreId === 'all'"
                @click="selectGenre('all')" rounded="lg" class="mb-2 mx-2" variant="tonal">
                <template v-slot:prepend>
                    <v-icon icon="mdi-apps"></v-icon>
                </template>
                <v-list-item-title class="font-weight-bold">全部書籍</v-list-item-title>
            </v-list-item>

            <v-divider class="my-2 mx-4"></v-divider>

            <!-- 分類列表 -->
            <div class="genre-list-container">
                <v-list-item v-for="genre in genres" :key="genre.genreId" :value="genre.genreId" active-color="primary"
                    :active="selectedGenreId === genre.genreId" @click="selectGenre(genre.genreId)" rounded="lg"
                    class="mb-1 mx-2 genre-item">
                    <template v-slot:prepend>
                        <v-icon :icon="selectedGenreId === genre.genreId ? 'mdi-leaf' : 'mdi-leaf-off'" size="small"
                            :color="selectedGenreId === genre.genreId ? 'primary' : 'grey'"></v-icon>
                    </template>
                    <v-list-item-title :class="{ 'font-weight-bold': selectedGenreId === genre.genreId }">
                        {{ genre.genreName }}
                    </v-list-item-title>
                </v-list-item>
            </div>
        </v-list>
    </v-card>
</template>

<style scoped>
.sticky-sidebar {
    position: sticky;
    top: 24px;
    max-height: calc(100vh - 48px);
    display: flex;
    flex-direction: column;
}

.genre-list-container {
    overflow-y: auto;
    flex: 1;
    scrollbar-width: thin;
    scrollbar-color: rgba(0, 0, 0, 0.2) transparent;
}
</style>
