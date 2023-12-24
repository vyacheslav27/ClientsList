package ru.clientslist

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.clientslist.data.ContactDao
import ru.clientslist.model.Contact

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    dao: ContactDao
) {
    val sheetShow = rememberSaveable { mutableStateOf(false) }
    val selectedContact = rememberSaveable { mutableStateOf<Contact?>(null) }

    val listState = rememberSaveable { mutableStateOf(emptyList<Contact>()) }

    LaunchedEffect(Unit) {
        listState.value = dao.selectAll()
    }

    val addAction = remember {
        { contact: Contact ->
            val id = dao.insert(contact)
            val newList = listState.value.toMutableList()
                .also { it.add(contact.copy(id = id)) }

            listState.value = newList
            sheetShow.value = false
        }
    }

    val removeAction = remember {
        { contact: Contact ->
            dao.delete(contact)
            val newList = listState.value.toMutableList()
                .also { it.removeIf { it.id == contact.id } }

            listState.value = newList
            selectedContact.value = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                actions = {
                    IconButton(onClick = {
                        sheetShow.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = ""
                        )
                    }
                }
            )
        },
        content = {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                itemsIndexed(listState.value) { index, item ->
                    ContactItem(index + 1, item) {
                        selectedContact.value = it
                    }
                    Divider()
                }
            }

            if (sheetShow.value) ModalBottomSheet(
                onDismissRequest = { sheetShow.value = false },
                sheetState = rememberModalBottomSheetState(true)
            ) {
                EditScreen {
                    addAction(it)
                }
            }

            selectedContact.value?.let { contact ->
                ModalBottomSheet(
                    onDismissRequest = { selectedContact.value = null },
                    sheetState = rememberModalBottomSheetState(true)
                ) {
                    DetailsScreen(contact) {
                        removeAction(it)
                    }
                }
            }
        }
    )


}

@Composable
fun DetailsScreen(
    contact: Contact,
    onRemove: (Contact) -> Unit
) {
    val data = listOf(
        R.string.name to contact.name,
        R.string.lastname to contact.lastname,
        R.string.email to contact.email,
        R.string.number to toNumberMask(contact.number)
    )

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(ScrollState(0))
    ) {
        data.forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp,
                        horizontal = 12.dp
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    fontSize = 20.sp,
                    text = stringResource(pair.first)
                )

                Text(
                    fontSize = 18.sp,
                    text = pair.second
                )
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            onClick = { onRemove(contact) }
        ) {
            Text(stringResource(R.string.delete))
        }
    }
}

@Composable
fun EditScreen(
    onContactSave: (Contact) -> Unit
) {
    val nameState = rememberSaveable { mutableStateOf("") }
    val lastnameState = rememberSaveable { mutableStateOf("") }
    val emailState = rememberSaveable { mutableStateOf("") }
    val numberState = rememberSaveable { mutableStateOf("") }

    val modifier = remember {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    }
    Column(
        Modifier
            .fillMaxSize()
            .padding(10.dp)
            .verticalScroll(ScrollState(0))
    ) {
        val isNameError = nameState.value.run { isBlank() && length < 2 }
        val isLastnameError = lastnameState.value.run { isBlank() && length < 2 }
        val isEmailError = !emailState.value.matches(Regex(RegexPattern.EMAIL))
        val isNumberError = numberState.value.length < 10

        Text(
            stringResource(R.string.new_contact),
            fontSize = 23.sp,
            fontStyle = FontStyle.Italic
        )

        TextField(
            modifier = modifier,
            value = nameState.value,
            label = { Text(stringResource(R.string.name)) },
            isError = isNameError,
            onValueChange = { nameState.value = it }
        )

        TextField(
            modifier = modifier,
            value = lastnameState.value,
            label = { Text(stringResource(R.string.lastname)) },
            isError = isLastnameError,
            onValueChange = { lastnameState.value = it }
        )

        TextField(
            modifier = modifier,
            value = emailState.value,
            label = { Text(stringResource(R.string.email)) },
            isError = isEmailError,
            onValueChange = { emailState.value = it }
        )

        TextField(
            modifier = modifier,
            value = numberState.value,
            label = { Text(stringResource(R.string.number)) },
            isError = isNumberError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = { mobileNumberFilter(it, Color.Gray) },
            onValueChange = { if (it.length <= 10) numberState.value = it }
        )

        Button(
            modifier = modifier,
            enabled = !isNameError && !isLastnameError && !isEmailError && !isNumberError,
            onClick = {
                val name = nameState.value
                val lastname = lastnameState.value
                val email = emailState.value
                val phone = numberState.value

                onContactSave(
                    Contact(
                        id = 0,
                        name = name,
                        lastname = lastname,
                        email = email,
                        number = phone
                    )
                )
            }
        ) {
            Text(stringResource(R.string.save))
        }
    }
}

@Composable
fun ContactItem(
    index: Int,
    contact: Contact,
    onClick: (Contact) -> Unit = {}
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 6.dp,
                horizontal = 8.dp
            )
            .clickable { onClick(contact) },
        fontSize = 18.sp,
        text = contact.run {
            listOf(name, lastname)
                .joinToString(
                    " ",
                    prefix = "$index. "
                )
        }
    )
}