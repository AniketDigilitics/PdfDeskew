import { ReportPageComponent } from './report/report-page.component';
import { UploadPageComponent } from './pages/UploadPageComponent/upload-page.component';
import { EditorPageComponent } from './pages/EditorPageComponent/editor-page.component';

export const routes = [
  { path: '', component: UploadPageComponent },
  { path: 'edit/:jobId', component: EditorPageComponent },
  { path: 'report/:jobId', component: ReportPageComponent }
];
