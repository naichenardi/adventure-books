import {Routes} from '@angular/router';
import {GamePageComponent} from './pages/game-page/game-page.component';
import {LibraryPageComponent} from './pages/library-page/library-page.component';

export const routes: Routes = [
	{
		path: '',
		component: LibraryPageComponent
	},
	{
		path: 'play',
		component: GamePageComponent
	},
	{
		path: 'play/:bookId',
		component: GamePageComponent
	},
	{
		path: '**',
		redirectTo: ''
	}
];
