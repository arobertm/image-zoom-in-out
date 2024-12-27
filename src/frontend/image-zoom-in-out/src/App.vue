<template>
  <div class="min-h-screen bg-cover bg-center" style="background-image: url('./github-background.png');">
    <nav class="fixed top-0 left-0 w-full bg-black bg-opacity-80 text-white shadow-lg z-50">
      <div class="container mx-auto flex justify-between items-center p-4">
        <h1 class="text-3xl font-bold text-red-500">_+image</h1>
        <div class="space-x-6">
          <RouterLink to="/upload" class="hover:underline text-lg text-white">Upload</RouterLink>
          <RouterLink to="/snmp" class="hover:underline text-lg text-white">SNMP Stats</RouterLink>
          <RouterLink to="/download" class="hover:underline text-lg text-white">Download</RouterLink>
        </div>
      </div>
    </nav>
    <main class="container mx-auto pt-24 p-4">
      <TitleComponent />
      <router-view />
    </main>
  </div>
</template>

